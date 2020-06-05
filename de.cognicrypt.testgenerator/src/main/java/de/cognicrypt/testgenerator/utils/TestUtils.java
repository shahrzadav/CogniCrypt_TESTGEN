package de.cognicrypt.testgenerator.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.ui.actions.FormatAllAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

import com.google.common.base.Defaults;

import de.cognicrypt.testgenerator.generator.TestGenerator;
import de.cognicrypt.utils.DeveloperProject;
import de.cognicrypt.utils.UIUtils;

public class TestUtils {
	
	static Logger debugLogger = Logger.getLogger(TestGenerator.class.getName());

	/**
	 * This method creates a empty JavaProject in the current workspace
	 * 
	 * @param projectName for the JavaProject
	 * @return new created JavaProject
	 * @throws CoreException
	 */
	public static IJavaProject createJavaProject(final String projectName) throws CoreException {

		debugLogger.info("Creating " + projectName + " project.");
		
		final IWorkspaceRoot workSpaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		deleteProject(workSpaceRoot.getProject(projectName));

		final IProject project = workSpaceRoot.getProject(projectName);
		project.create(null);
		project.open(null);

		final IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] {JavaCore.NATURE_ID});
		project.setDescription(description, null);

		final IJavaProject javaProject = JavaCore.create(project);

		final IFolder binFolder = project.getFolder("bin");
		binFolder.create(false, true, null);
		javaProject.setOutputLocation(binFolder.getFullPath(), null);

		final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		final IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		final LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		for (final LibraryLocation element : locations) {
			entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		}
		// add libs to project class path
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

		final IFolder sourceFolder = project.getFolder("src");
		sourceFolder.create(false, true, null);

		final IPackageFragmentRoot packageRoot = javaProject.getPackageFragmentRoot(sourceFolder);
		final IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		final IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(packageRoot.getPath());
		javaProject.setRawClasspath(newEntries, null);
		
		debugLogger.info("Finished creating " + projectName + " project.");

		return javaProject;
	}
	
	/**
	 * This method deletes a JavaProject from the Workspace/hard drive
	 * 
	 * @param project Java project that will be deleted
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public static void deleteProject(final IProject project) throws CoreException {
		if(project.exists()) {
			debugLogger.info("Deleting existing project.");
			project.delete(true, true, null);
			debugLogger.info("Finished deletion.");
		}
	}
	
	/**
	 * This method creates a package with a java class into a JavaProject <<<<<<< HEAD
	 * 
	 * @param project JavaProject in which the new Java class will be generated
	 * @param packageName package in which the new Java class will be generated
	 * @param className name of the new Java class
	 * @throws JavaModelException
	 */
	public static IResource generateJavaClassInJavaProject(final IJavaProject project, final String packageName, final String className) throws JavaModelException {

		final IPackageFragment pack = project.getPackageFragmentRoot(project.getProject().getFolder("src")).createPackageFragment(packageName, false, null);
		final String source = "public class " + className + " {\n\n}\n";
		final StringBuffer buffer = new StringBuffer();
		buffer.append("package " + pack.getElementName() + ";\r\n\r\n");
		buffer.append(source);
		ICompilationUnit unit = pack.createCompilationUnit(className + ".java", buffer.toString(), false, null);
		return unit.getUnderlyingResource();
	}
	
	public static String retrieveOnlyClassName(String className) {
		String[] values = className.split("\\.");
		return values[values.length-1];
	}
	
	public static void cleanUpProject(DeveloperProject project) throws CoreException {
		project.refresh();
		final ICompilationUnit[] units = project.getPackagesOfProject("jca").getCompilationUnits();

		if (units.length > 0 && units[0].getResource().getType() == IResource.FILE) {
			IFile genClass = (IFile) units[0].getResource();
			IDE.openEditor(UIUtils.getCurrentlyOpenPage(), genClass);
			IEditorPart editor = UIUtils.getCurrentlyOpenPage().getActiveEditor();
			final FormatAllAction faa = new FormatAllAction(editor.getSite());
			faa.runOnMultiple(units);
		} else {
			debugLogger.info("No files found.");
		}
	}
	
	public static String getDefaultValue(String type) {

		switch(type) {
			case "byte":
				return Defaults.defaultValue(Byte.TYPE).toString();
			case "short":
				return Defaults.defaultValue(Short.TYPE).toString();
			case "int":
				return Defaults.defaultValue(Integer.TYPE).toString();
			case "long":
				return Defaults.defaultValue(Long.TYPE).toString();
			case "float":
				return Defaults.defaultValue(Float.TYPE).toString();
			case "double":
				return Defaults.defaultValue(Double.TYPE).toString();
			case "boolean":
				return Defaults.defaultValue(Boolean.TYPE).toString();
			default:
				throw new IllegalArgumentException("Type " + type + " not supported");
		}
	}
}