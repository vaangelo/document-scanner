/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class PdfsandwichOCREngine extends ProcessOCREngine {
    private final static String PDFSANDWICH_DEFAULT = "pdfsandwich";
    private final static Logger LOGGER = LoggerFactory.getLogger(PdfsandwichOCREngine.class);
    private final static String INPUT_TEMP_FILE_PREFIX = "pdfsandwich-ocr-engine-input";

    public PdfsandwichOCREngine() {
        super(PDFSANDWICH_DEFAULT);
    }

    /**
     * Processes {@code image} with {@code pdfsandwich} including creation of
     * temporary input and output files and reading data back from the latter
     * since {@code pdfsandwich} seems to be incapable of reading from stdin or
     * writing to stdout.
     * @param image
     * @return
     */
    @Override
    protected String recognizeImage0(BufferedImage image) {
        try {
            LOGGER.debug(String.format("using prefix '%s' for pdfsandwich input temp file name", INPUT_TEMP_FILE_PREFIX));
            File inputFile = File.createTempFile(INPUT_TEMP_FILE_PREFIX,
                    null);
            ImageIO.write(image, "png", inputFile);
            ProcessBuilder pdfsandwichProcessBuilder = new ProcessBuilder(this.getBinary(),
                    "-noimage",
                    inputFile.getAbsolutePath()
            ).redirectOutput(ProcessBuilder.Redirect.PIPE);
                //does not expect an output file, but create an output named
                //after a scheme
            Process pdfsandwichProcess = pdfsandwichProcessBuilder.start();
            setBinaryProcess(pdfsandwichProcess);
            int pdfsandwichProcessExitValue = pdfsandwichProcess.waitFor();
            if(pdfsandwichProcessExitValue != 0) {
                //tesseractProcess.destroy might cause IOException, but
                //termination with exit value != 0 might occur as well
                return null;
            }
            throw new UnsupportedOperationException("Wait for https://github.com/Manawyrm/pdfsandwich-txt-output to pull fix for 'make install' error (see https://github.com/Manawyrm/pdfsandwich-txt-output/issues/2 for details)");
        } catch(InterruptedException ex) {
            //InterruptedException is an IOException
            return null; //might at one point be thrown due to Process.destroy
                    //cancelation
        } catch (IOException ex) {
            if(ex.getMessage().equals("Stream closed")) {
                return null; //result of Process.destroy
            }
            throw new RuntimeException(ex);
        }
    }
}