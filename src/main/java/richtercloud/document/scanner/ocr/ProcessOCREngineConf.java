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

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import richtercloud.document.scanner.ifaces.OCREngineConf;

/**
 *
 * @author richter
 */
public abstract class ProcessOCREngineConf implements OCREngineConf {
    private static final long serialVersionUID = 1L;
    private String binary;

    public ProcessOCREngineConf(String binary) {
        this.binary = binary;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    @Override
    public String toString() {
        ToStringBuilder toStringBuilder = new ReflectionToStringBuilder(this,
                new RecursiveToStringStyle());
        String retValue = toStringBuilder.toString();
        return retValue;
    }
}
