package pdfhandleapp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class GetImageToken extends PDFStreamEngine {

    Map<Integer, ImageIcon> imageic;
    int imgToReadLimit;

    public GetImageToken(int imgToReadLimit) throws IOException {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
        imageic = new TreeMap<Integer, ImageIcon>();
        this.imgToReadLimit=imgToReadLimit;
    }

    /**
     * Esto se utiliza para manejar una operación.
     *
     * @param operator La operación a realizar.
     * @param operandos La lista de argumentos.
     *
     * @ throws IOException Si hay un error al procesar la operación.
     */
    protected void processOperator(Operator operador, List<COSBase> operands) throws IOException {
        String operacion = operador.getName();

        if ("Do".equals(operacion)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xobject;
                if (objectName.getName().contains("img")) {
                    int imgen = Integer.valueOf(objectName.getName().replaceFirst("img", ""));
                    if (imgen % 2 == 0 && imgen >= 2 && imgen <= imgToReadLimit) {
                        imageic.put(imgen, new ImageIcon(image.getImage()));
                        final OutputStream stream = new FileOutputStream(new File(objectName.getName() + ".jpg"));
                        ImageIOUtil.writeImage(image.getImage(), "jpeg", (OutputStream) stream, 75, 1);
                    }
                }
            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operador, operands);
        }
    }

    public Map<Integer, ImageIcon> getImageic() {
        return imageic;
    }

}
