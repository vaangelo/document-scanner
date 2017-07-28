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
package richtercloud.document.scanner.gui;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;
import richtercloud.document.scanner.components.annotations.Invisible;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.validation.tools.FieldRetrievalException;

/**
 *
 * @author richter
 */
public class DocumentScannerFieldRetriever extends JPACachedFieldRetriever {
    private final DocumentScannerConf documentScannerConf;

    public DocumentScannerFieldRetriever(DocumentScannerConf documentScannerConf) {
        this.documentScannerConf = documentScannerConf;
    }

    @Override
    public List<Field> retrieveRelevantFields(Class<?> entityClass) throws FieldRetrievalException {
        List<Field> retValue = documentScannerConf.getFieldOrderMap().get(entityClass);
        ListIterator<Field> retValueItr = retValue.listIterator();
        while(retValueItr.hasNext()) {
            Field retValueNxt = retValueItr.next();
            if(retValueNxt.getAnnotation(Invisible.class) != null) {
                retValueItr.remove();
            }
        }
        //it shouldn't matter that we're manipulating
        //DocumentScannerConf.fieldOrderMap directly since noone would ever want
        //to have @Invisible fields in the ordering
        return retValue;
    }
}
