package org.brijframework.context;

import static org.brijframework.support.config.SupportConstants.APPLICATION_BOOTSTRAP_CONFIG_FILES;
import static org.brijframework.support.config.SupportConstants.APPLICATION_BOOTSTRAP_CONFIG_PATHS;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.brijframework.asm.context.AbstractBootstrapContext;
import org.brijframework.asm.factories.FileFactory;
import org.brijframework.config.EnvConfigration;
import org.brijframework.context.config.ApplicationConfigration;
import org.brijframework.support.config.ApplicationBootstrap;
import org.brijframework.support.enums.ResourceType;
import org.brijframework.util.objects.PropertiesUtil;
import org.brijframework.util.reflect.AnnotationUtil;
import org.brijframework.util.reflect.InstanceUtil;
import org.brijframework.util.reflect.ReflectionUtils;
import org.brijframework.util.resouces.YamlUtil;

public class ApplicationContext extends AbstractBootstrapContext {
	
	private EnvConfigration configration;
	
	public ApplicationContext() {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		try {
			ReflectionUtils.getClassListFromExternal().forEach(cls -> {
				if (ModuleContext.class.isAssignableFrom(cls) && InstanceUtil.isAssignable(cls)) {
					register((Class<? extends ModuleContext>) cls);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			ReflectionUtils.getClassListFromInternal().forEach(cls -> {
				if (ModuleContext.class.isAssignableFrom(cls) && InstanceUtil.isAssignable(cls)) {
					register((Class<? extends ModuleContext>) cls);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		System.err.println("=============================ApplicationContext startup==============================");
		super.start();
		System.err.println("=============================ApplicationContext started==============================");
	}

	@Override
	public void stop() {
		System.err.println("=============================ApplicationContext startup==============================");
		
		System.err.println("=============================ApplicationContext started==============================");

	}
	
	public void load() {
		System.err.println("=============================Application Configration startup=========================");
		
		EnvConfigration configration=getConfigration();
		//configration.getProperties().putAll(System.getProperties());
		findAnnotationConfig(configration);
		findFileLocateConfig(configration);
		if(!configration.getProperties().containsKey(APPLICATION_BOOTSTRAP_CONFIG_PATHS)) {
			configration.getProperties().put(APPLICATION_BOOTSTRAP_CONFIG_PATHS, APPLICATION_BOOTSTRAP_CONFIG_FILES);
		}
		loadFileLocateConfig(configration);
		configration.getProperties().entrySet().stream().sorted((entry1,entry2)->((String)entry1.getKey()).compareToIgnoreCase(((String)entry2.getKey()))).forEach(entry->{
			System.err.println(entry.getKey()+"="+entry.getValue());
		});
		System.err.println("=============================Application Configration started==========================");
	}
	
	
	protected void findAnnotationConfig(EnvConfigration configration) {
		if(configration.getProperties().containsKey(APPLICATION_BOOTSTRAP_CONFIG_PATHS)) {
			return;
		}
		try {
			ReflectionUtils.getClassListFromInternal().forEach(cls -> {
				if (cls.isAnnotationPresent(ApplicationBootstrap.class)) {
					ApplicationBootstrap config=(ApplicationBootstrap) AnnotationUtil.getAnnotation(cls, ApplicationBootstrap.class);
					List<File> files=new ArrayList<>();
					FileFactory.getResources(Arrays.asList(config.paths().split("\\|"))).forEach(file -> {
						System.out.println("Loading Application = "+file);
						files.add(file);
					});
					configration.getProperties().put(APPLICATION_BOOTSTRAP_CONFIG_PATHS, files);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void findFileLocateConfig(EnvConfigration configration) {
		if(configration.getProperties().containsKey(APPLICATION_BOOTSTRAP_CONFIG_PATHS)) {
			return;
		}
		try {
			FileFactory.getResources(Arrays.asList(APPLICATION_BOOTSTRAP_CONFIG_FILES.split("\\|"))).forEach(file -> {
				configration.getProperties().put(APPLICATION_BOOTSTRAP_CONFIG_PATHS, file.getPath());
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected void loadFileLocateConfig(EnvConfigration configration) {
		try {
			@SuppressWarnings("unchecked")
			List<File> files=(List<File>) configration.getProperties().get(APPLICATION_BOOTSTRAP_CONFIG_PATHS);
			for(File filePath :files) {
			System.err.println(APPLICATION_BOOTSTRAP_CONFIG_PATHS+"="+filePath);
			if(!filePath.exists()) {
				System.err.println("Env configration file not found.");
				continue ;
			}
			if(filePath.toString().endsWith(ResourceType.PROP)) {
				configration.getProperties().putAll(PropertiesUtil.getProperties(filePath));
			}
			if(filePath.toString().endsWith(ResourceType.YML)||filePath.toString().endsWith(ResourceType.YAML)) {
				configration.getProperties().putAll(YamlUtil.getEnvProperties(filePath));
			}
			this.getProperties().putAll(configration.getProperties());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public EnvConfigration getConfigration() {
		if(this.configration==null) {
			this.configration=new ApplicationConfigration();
		}
		return this.configration;
	}

}
