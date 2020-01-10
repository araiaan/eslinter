package lint;

import static burp.BurpExtender.extensionConfig;
import static burp.BurpExtender.log;
import static burp.BurpExtender.mainTab;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import linttable.LintResult;
import utils.Exec;
import utils.StringUtils;

/**
 * BeautifyTask
 */
public class BeautifyTask implements Runnable {

    // private Beautify beautifier;
    private String data;
    private Metadata metadata;
    private String storagePath = "";

    public BeautifyTask(String data, Metadata metadata, String storagePath) {

        this.data = data;
        this.metadata = metadata;
        this.storagePath = storagePath;
        log.debug("Created a new BeautifyTask.\nmetadata\n%s\nStorage path: %s",
            metadata.toString(), storagePath);
    }

    @Override
    public void run() {

        Exec beautify = null, linter = null;
        String status = "", host = "";
        int numOfFindings = 0;

        try {

            // Get the host, needed later to add to the table.
            host = StringUtils.getURLHost(metadata.getURL());

            // Filename will be "filename_from_URL[minus extension]-[hash].js".
            String jsFileName = StringUtils.getURLBaseName(metadata.getURL());
            jsFileName = jsFileName.replaceAll("=", "-");
            if (!StringUtils.isEmpty(jsFileName)) {
                // If the URL does not end in a file jsFIleName will be empty.
                // If it's not empty, we add the "-" to it.
                jsFileName = jsFileName.concat("-");
            }

            // Attach the hash and the extension.
            jsFileName = jsFileName.concat(metadata.getHash());

            // Add the js extension.
            String jsFilePath = FilenameUtils.concat(storagePath, jsFileName.concat(".js"));
            // Create a File.
            File jsFile = new File(jsFilePath);

            // Create the metadata string.
            StringBuilder sb = new StringBuilder(metadata.toCommentString());
            // Add the extracted JavaScript.
            sb.append(data);
            
            // Eslint and js-beautify directories are the same because they are
            // installed in the same location.
            String eslintDirectory = FilenameUtils.getFullPath(extensionConfig.eslintBinaryPath);

            // Write the contents to the file.
            FileUtils.writeStringToFile(jsFile, sb.toString(), "UTF-8");
            // Now we have a file with metadata and not-beautified JavaScript.
            // js-beautify -f [filename] -r
            // -r or --replace replace the same file with the beautified content
            // this will hopefully keep the metadata string intact (because it's
            // a comment).

            String[] beautifyArgs = new String[] {
                "-f", jsFilePath, "-r"
            };
            beautify = new Exec(
                extensionConfig.jsBeautifyBinaryPath,
                beautifyArgs,
                eslintDirectory
            );

            // int exitVal = beautify.exec();
            beautify.exec();
            log.debug("Executing %s", beautify.getCommandLine());
            log.debug("Output: %s", beautify.getStdOut());

            // Now we can read the file to get the beautified data if needed.
            // Execute ESLint with Exec on the file.

            // Create the output filename and path.
            // Output filename is the same as the original filename with "-out".
            String eslintResultFileName = jsFileName.concat("-out.js");
            String eslintResultFilePath = 
                FilenameUtils.concat(extensionConfig.eslintOutputPath, eslintResultFileName);

            String[] linterArgs = new String[] {
                "-c", extensionConfig.eslintConfigPath,
                "-f", "codeframe",
                "--no-color",
                // "-o", eslintResultFileName, // Use this if we want to create the output file manually.
                "--no-inline-config",
                jsFilePath
            };
            
            linter = new Exec(
                extensionConfig.eslintBinaryPath,
                linterArgs,
                eslintDirectory
            );

            log.debug("Executing %s", linter.getCommandLine());
            // exitVal = linter.exec();
            linter.exec();
            String result = linter.getStdOut();

            // Add the metadata to the output file.
            sb = new StringBuilder(metadata.toCommentString());
            sb.append(result);

            FileUtils.writeStringToFile(
                new File(eslintResultFilePath), sb.toString(), "UTF-8"
            );
            
            // Regex to separate the findings.
            // (.*?)\n\n\n

            String ptrn = "(.*?)\n\n\n";
            int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;
            Pattern pt = Pattern.compile(ptrn, flags);
            Matcher mt = pt.matcher(result);

            // Now each item in the matcher is a separate finding.
            // TODO Do something with each finding.
            numOfFindings = (int) mt.results().count();

            log.debug("Results file: %s", eslintResultFilePath);
            log.debug("Input file: %s", jsFilePath);
            log.debug("----------");

        } catch (Exception e) {

            status = StringUtils.getStackTrace(e);

            if (beautify != null) {
                if (StringUtils.isNotEmpty(beautify.getStdErr())) {
                    status += beautify.getStdErr();
                }
            }

            if (linter != null) {
                if (StringUtils.isNotEmpty(linter.getStdErr())) {
                    status += linter.getStdErr();
                }
            }
            log.error(status);

        } finally {
            // Create the LintResult and add to the table.
            final LintResult lr = new LintResult(
                host,
                metadata.getURL(),
                status,
                numOfFindings
            );

            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
					mainTab.lintTable.add(lr);
                }
            });
            log.debug("Added the request to the table.");
        }
    }

    public String toString() {
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
    }
}