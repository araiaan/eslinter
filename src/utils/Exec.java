package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;

/**
 * Exec
 */
public class Exec {

    // TODO Use this?
    private String command;
    private String[] arguments;
    private String workingDirectory;
    private String stdOut = "";
    private String stdErr = "";
    private boolean done = false;
    private CommandLine cmdLine;

    public Exec(String cmd, String[] args, String workDir) {
        command = cmd;
        arguments = args;
        workingDirectory = workDir;

        // Add main command.
        cmdLine = new CommandLine(command);
        // Add arguments.
        for (String arg : arguments) {
            cmdLine.addArgument(arg);
        }
    }

    public int exec() throws ExecuteException, IOException {

        DefaultExecutor executor = new DefaultExecutor();

        // How to get both stdout and stderr.
        // https://stackoverflow.com/a/34571800
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout, stderr);
        executor.setStreamHandler(psh);
        executor.setWorkingDirectory(new File(workingDirectory));
        // ESLint returns 2 on parsing errors so we do not want an exception.
        executor.setExitValues(new int[] {0, 2});
        int exitValue = executor.execute(cmdLine);
        done = true;
        stdOut = stdout.toString();
        stdErr = stderr.toString();
        return exitValue;
    }

    public String getCommandLine() {
        return cmdLine.toString();
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }
    
    public static String execute(String workingDir, String... commands) throws IOException {
        ArrayList<String> cmd = new ArrayList<String>();
        String[] cmdPrompt = new String[] {
            "cmd.exe", "/c"
        };
        cmd.addAll(Arrays.asList(cmdPrompt));
        cmd.addAll(Arrays.asList(commands));
        // cmd.add("cd"); // This helps us figure out where we are.
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(workingDir));
        Process p = pb.start();
        String output = IOUtils.toString(p.getInputStream(), "UTF-8");
        String error = IOUtils.toString(p.getErrorStream(), "UTF-8");

        // TODO Find a better way of propagating the error results. Should we
        // throw an exception instead?
        // Make a custom exception and throw it with the message from error?
        String result = "";
        if (StringUtils.isNotEmpty(output)) result += output;
        if (StringUtils.isNotEmpty(error)) result += "---" + output;
        
        return result;

    }
}