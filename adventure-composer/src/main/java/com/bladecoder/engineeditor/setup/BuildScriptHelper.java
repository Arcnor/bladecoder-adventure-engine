package com.bladecoder.engineeditor.setup;


import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import com.bladecoder.engineeditor.setup.DependencyBank.ProjectType;
import com.bladecoder.engineeditor.utils.Versions;

public class BuildScriptHelper {

	private static int indent = 0;

	public static void addBuildScript(List<ProjectType> projects, BufferedWriter wr) throws IOException {
		write(wr, "buildscript {");
		//repos
		write(wr, "repositories {");
		write(wr, DependencyBank.mavenCentral);
		if (projects.contains(ProjectType.HTML)) {
			write(wr, DependencyBank.jCenter);
		}
		write(wr, "}");
		//dependencies
		write(wr, "dependencies {");
		if (projects.contains(ProjectType.HTML)) {
			write(wr, "classpath '" + DependencyBank.gwtPluginImport + Versions.getGwtGradlePluginVersion() + "'");
		}
		if (projects.contains(ProjectType.ANDROID)) {
			write(wr, "classpath '" + DependencyBank.androidPluginImport + Versions.getAndroidGradlePluginVersion() + "'");
		}
		if (projects.contains(ProjectType.IOS)) {
			write(wr, "classpath '" + DependencyBank.roboVMPluginImport + Versions.getROBOVMGradlePluginVersion() + "'");
		}
		write(wr, "}");
		write(wr, "}");
		space(wr);
	}

	public static void addAllProjects(BufferedWriter wr) throws IOException {
		write(wr, "allprojects {");
		write(wr, "apply plugin: \"eclipse\"");
		write(wr, "apply plugin: \"idea\"");
		space(wr);
		write(wr, "version = '1.0'");
		
	    write(wr, "if(project.hasProperty('passed_version'))\n        version = passed_version");
		
		write(wr, "ext {");
		write(wr, "appName = '%APP_NAME%'");
		write(wr, "bladeEngineVersion = '" + Versions.getVersion() + "'");
		write(wr, "gdxVersion = '" + Versions.getLibgdxVersion() + "'");
		write(wr, "roboVMVersion = '" + Versions.getRoboVMVersion() + "'");
		write(wr, "}");
		space(wr);
		write(wr, "repositories {");
		write(wr, DependencyBank.mavenCentral);
		write(wr, "maven { url \"" + DependencyBank.libGDXSnapshotsUrl + "\" }");
		write(wr, "maven { url \"" + DependencyBank.libGDXReleaseUrl + "\" }");
		write(wr, "}");
		write(wr, "}");
	}

	public static void addProject(ProjectType project, List<Dependency> dependencies, BufferedWriter wr) throws IOException {
		space(wr);
		write(wr, "project(\":" + project.getName() + "\") {");
		for (String plugin : project.getPlugins()) {
			write(wr, "apply plugin: \"" + plugin + "\"");
		}
		space(wr);
		addConfigurations(project, wr);
		space(wr);
		addDependencies(project, dependencies, wr);
		write(wr, "}");
	}

	private static void addDependencies(ProjectType project, List<Dependency> dependencyList, BufferedWriter wr) throws IOException {
		write(wr, "dependencies {");
		if (!project.equals(ProjectType.CORE)) {
			write(wr, "compile project(\":" + ProjectType.CORE.getName() + "\")");
		}
		for (Dependency dep : dependencyList) {
			if (dep.getDependencies(project) == null) continue;
			for (String moduleDependency : dep.getDependencies(project)) {
				if (moduleDependency == null) continue;
				if ((project.equals(ProjectType.ANDROID)) && moduleDependency.contains("native")) {
					write(wr, "natives \"" + moduleDependency + "\"");
				} else {
					if(moduleDependency.startsWith("fileTree("))
						write(wr, "compile " + moduleDependency);
					else
						write(wr, "compile \"" + moduleDependency + "\"");
				}
			}
		}
		write(wr, "}");
	}

	private static void addConfigurations(ProjectType project, BufferedWriter wr) throws IOException {
		if (project.equals(ProjectType.IOS) || project.equals(ProjectType.ANDROID)) {
			write(wr, "configurations { natives }");
		}
	}

	private static void write(BufferedWriter wr, String input) throws IOException {
		int delta = StringUtils.countMatches(input, '{') - StringUtils.countMatches(input, '}');
		indent += delta *= 4;
		indent = clamp(indent);
		if (delta > 0) {
			wr.write(StringUtils.repeat(" ", clamp(indent - 4)) + input + "\n");
		} else if (delta < 0) {
			wr.write(StringUtils.repeat(" ", clamp(indent)) + input + "\n");
		} else {
			wr.write(StringUtils.repeat(" ", indent) + input + "\n");
		}
	}

	private static void space(BufferedWriter wr) throws IOException {
		wr.write("\n");
	}

	private static int clamp(int indent) {
		if (indent < 0) {
			return 0;
		}
		return indent;
	}

	static class StringUtils {

		public static int countMatches(String input, char match) {
			int count = 0;
			for (int i = 0; i < input.length(); i++) {
				if (input.charAt(i) == match) {
					count++;
				}
			}
			return count;
		}

		public static String repeat(String toRepeat, int count) {
			String repeat = "";
			for (int i = 0; i < count; i++) {
				repeat += toRepeat;
			}
			return repeat;
		}
	}

}
