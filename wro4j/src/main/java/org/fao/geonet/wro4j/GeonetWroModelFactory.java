package org.fao.geonet.wro4j;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.StopWatch;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Creates model of views to javascript and css.
 * <p/>
 * User: Jesse
 * Date: 11/22/13
 * Time: 8:28 AM
 */
public class GeonetWroModelFactory implements WroModelFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GeonetWroModelFactory.class);

    private static final String WRO_SOURCES_KEY = "wroSources";
    @Inject
    private ReadOnlyContext context;

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public WroModel create() {
        final StopWatch stopWatch = new StopWatch("Create Wro Model using Geonetwork");
        try {
            stopWatch.start("createModel");
            final Element element = Xml.loadFile(getSourcesXmlFile());
            final List<Element> jsSources = element.getChildren("jsSource");

            final WroModel model = new WroModel();

            final ClosureRequireDependencyManager dependencyManager = configureJavascripDependencyManager(jsSources);

            addJavascriptGroups(model, dependencyManager);

            final List<Element> cssSources = element.getChildren("cssSource");
            addCssGroups(model, cssSources);

            return model;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JDOMException e) {
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
            LOG.debug(stopWatch.prettyPrint());
        }
    }

    private void addCssGroups(final WroModel model, final List<Element> cssSources) {
        for (Element cssSource : cssSources) {
            ResourceDesc desc = parseSource(cssSource);

            for (File file : desc.files("css")) {
                final Group group = new Group(Files.getNameWithoutExtension(file.getName()));
                Resource resource = new Resource();
                resource.setMinimize(true);
                resource.setType(ResourceType.CSS);
                resource.setUri(file.toURI().toString());
                model.addGroup(group);
            }
        }
        //To change body of created methods use File | Settings | File Templates.
    }

    private void addJavascriptGroups(final WroModel model, final ClosureRequireDependencyManager dependencyManager) {
        for (String moduleId : dependencyManager.getAllModuleIds()) {
            Group group = new Group(moduleId);

            final Collection<ClosureRequireDependencyManager.Node> deps =
                    dependencyManager.getTransitiveDependenciesFor(moduleId, true);

            for (ClosureRequireDependencyManager.Node dep : deps) {
                Resource resource = new Resource();
                resource.setMinimize(true);
                resource.setType(ResourceType.JS);
                resource.setUri(dep.path);
                group.addResource(resource);
            }

            model.addGroup(group);
        }
    }

    private ClosureRequireDependencyManager configureJavascripDependencyManager(final List<Element> jsSources) throws IOException {
        ClosureRequireDependencyManager depManager = new ClosureRequireDependencyManager();

        for (Element jsSource : jsSources) {
            ResourceDesc desc = parseSource(jsSource);
            for (File file : desc.files("js")) {
                String path = desc.relativePath + file.getPath().substring(desc.finalPath.length());
                depManager.addFile(path, file);
            }
        }

        depManager.validateGraph();

        return depManager;
    }

    private ResourceDesc parseSource(final Element sourceEl) {
        ResourceDesc desc = new ResourceDesc();

        desc.relativePath = sourceEl.getAttributeValue("webapp");
        desc.pathOnDisk = sourceEl.getAttributeValue("pathOnDisk");
        if (context.getServletContext() != null) {
            desc.finalPath = context.getServletContext().getRealPath(desc.relativePath);
        } else {
            desc.finalPath = new File(desc.pathOnDisk, desc.relativePath).getPath();
        }

        if (!desc.relativePath.endsWith(File.separator)) {
            desc.relativePath += File.separator;
        }

        desc.root = new File(desc.finalPath);

        return desc;
    }

    public String getSourcesXmlFile() {
        final String sourcesRawProperty = getConfigProperties().getProperty(WRO_SOURCES_KEY);
        if (context.getServletContext() != null) {
            final String[] split = sourcesRawProperty.split("WEB-INF/", 2);
            if (split.length == 2) {
                final String path = context.getServletContext().getRealPath("/WEB-INF/" + split[1]);
                if (path != null) {
                    return path;
                }
            }
        }
        return sourcesRawProperty;
    }

    protected Properties getConfigProperties() {
        return null;
    }


    private static class ResourceDesc {
            String relativePath;
            String pathOnDisk;
            String finalPath;
            File root;

            public Iterable<File> files(final String extToCollect) {
                final Predicate<File> filterByExt = new Predicate<File>() {
                    @Override
                    public boolean apply(@Nullable final File input) {
                        if (input != null) {
                            return input.getName().endsWith("." + extToCollect);
                        } else {
                            return false;
                        }
                    }
                };

                return Files.fileTreeTraverser().breadthFirstTraversal(root).filter(filterByExt);

            }
        }
    }
