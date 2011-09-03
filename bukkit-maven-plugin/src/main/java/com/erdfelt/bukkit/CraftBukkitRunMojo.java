package com.erdfelt.bukkit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.os.Os;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * Run the project, that has a plugin.yml present, as a plugin in the CraftBukkit server.
 * 
 * @goal run
 * @requiresProject true
 * @phase install
 */
public class CraftBukkitRunMojo extends AbstractMojo {
    /**
     * Maven Project.
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * The home of your CraftBukkit test server, where the worlds, configuration, and plugins directory are.
     * 
     * @parameter expression="${craftbukkit.home}" default-value="${project.basedir}/craftbukkit-server"
     * @required
     */
    private File           craftbukkitHome;

    /**
     * The project jarfile
     * 
     * @parameter expression="${project.build.directory}/${project.build.finalName}.jar"
     */
    private File           jarfile;

    private void assertResourceExists(String path) throws MojoExecutionException {
        List<String> checkedPaths = new ArrayList<String>();

        @SuppressWarnings("unchecked")
        List<Resource> resources = project.getResources();
        for (Resource res : resources) {
            File resFile = new File(res.getDirectory(), path);
            if (resFile.exists()) {
                getLog().debug("Found '" + path + "' at " + resFile.getAbsolutePath());
                return; // found it
            }
            checkedPaths.add(resFile.getAbsolutePath());
        }

        StringBuilder err = new StringBuilder();
        err.append("Cannot run CraftBukkit on project that isn't a Bukkit Plugin.");
        err.append("\nMissing plugin.yml file in the root of one of your project's defined resource directories:");
        for (String checked : checkedPaths) {
            err.append("\n  ").append(checked);
        }

        throw new MojoExecutionException(err.toString());

    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        assertResourceExists("plugin.yml");
        // TODO: assertProjectJarExists();

        getLog().info("Project jar: " + jarfile.getAbsolutePath() + " (exists:" + jarfile.exists() + ")");

        if (craftbukkitHome.exists() == false) {
            if (craftbukkitHome.mkdirs() == false) {
                throw new MojoExecutionException("Unable to create craftbukkit.home directory: " + craftbukkitHome);
            }
        }
        getLog().info("CraftBukkit Home: " + craftbukkitHome);

        File craftBukkitJarSrc = findJar(org.bukkit.craftbukkit.Main.class);
        getLog().info("CraftBukkit JAR: " + craftBukkitJarSrc);

        File craftBukkitJarDest = new File(craftbukkitHome, craftBukkitJarSrc.getName());
        try {
            FileUtils.copyFile(craftBukkitJarSrc, craftBukkitJarDest);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy craftbukkit jar\nFrom: " + craftBukkitJarSrc + "\nTo: "
                    + craftBukkitJarDest, e);
        }

        try {
            List<String> cmds = new ArrayList<String>();
            cmds.add(getJavaExecutable());
            cmds.add("-Xincgc");
            cmds.add("-Xmx1G");
            cmds.add("-jar");
            cmds.add(craftBukkitJarDest.getAbsolutePath());
            cmds.add("--nojline");

            ProcessBuilder pb = new ProcessBuilder(cmds);
            if (getLog().isDebugEnabled()) {
                StringBuilder dbg = new StringBuilder();
                for (String arg : cmds) {
                    if (dbg.length() > 0) {
                        dbg.append(" ");
                    }
                    dbg.append(arg);
                }
                getLog().debug("Command Line: " + dbg);
            }
            pb.directory(craftbukkitHome);
            pb.redirectErrorStream(true);

            Process pid = pb.start();
            new PipeHandler(pid.getInputStream(), System.out).start();
            new PipeHandler(System.in, pid.getOutputStream()).start();
            int exitCode = pid.waitFor();
            getLog().debug("Exit code: " + exitCode);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to execute CraftBukkit server", e);
        }
    }

    private File findJar(Class<?> clazz) throws MojoExecutionException {
        String classname = "/" + clazz.getName().replace(".", "/") + ".class";
        getLog().debug("Looking for resource: " + classname);
        URL url = this.getClass().getResource(classname);
        getLog().debug("Found " + url);

        if (url == null) {
            throw new MojoExecutionException("Unable to find class: " + clazz.getName());
        }

        try {
            URI uri = url.toURI();
            if (!uri.isAbsolute()) {
                throw new MojoExecutionException("Unable to find jar for class: " + clazz.getName());
            }
            if (!"jar".equals(uri.getScheme())) {
                throw new MojoExecutionException("Unexpected scheme for jar file: " + uri);
            }
            String ssp = uri.getSchemeSpecificPart();
            ssp = ssp.replaceFirst("!/.*", "");
            getLog().debug("Found SSP: " + ssp);
            URI sspuri = URI.create(ssp);
            File file = new File(sspuri);
            return file;
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Unable to parse url: " + url);
        }
    }

    private String getJavaExecutable() throws IOException {
        String javaCommand = "java" + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".exe" : "");
        String javaHome = System.getProperty("java.home");
        getLog().debug("java.home = " + javaHome);
        File javaExe;
        if (Os.isName("Mac OS X")) {
            javaExe = new File(javaHome + File.separator + "bin" + javaCommand);
        } else {
            javaExe = new File(javaHome + File.separator + ".." + File.separator + "bin" + javaCommand);
        }

        // Try to find the JAVA_HOME environment variable.
        if (!javaExe.isFile()) {
            Properties env = CommandLineUtils.getSystemEnvVars();
            javaHome = env.getProperty("JAVA_HOME");
            if (StringUtils.isEmpty(javaHome)) {
                throw new IOException("The environment variable JAVA_HOME is not correctly set.");
            }
            if (!new File(javaHome).isDirectory()) {
                throw new IOException("The environment variable JAVA_HOME=" + javaHome
                        + " doesn't exist or is not a valid directory.");
            }

            javaExe = new File(env.getProperty("JAVA_HOME") + File.separator + "bin", javaCommand);
        }

        if (!javaExe.isFile()) {
            throw new IOException("The java executable '" + javaExe
                    + "' doesn't exist or is not a file. Verify the JAVA_HOME environment variable.");
        }

        return javaExe.getAbsolutePath();
    }
}
