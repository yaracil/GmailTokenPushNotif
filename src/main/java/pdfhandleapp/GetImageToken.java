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
import java.util.List;
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

    ImageIcon imageic;

    public GetImageToken() throws IOException {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
        imageic = null;
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
                if (objectName.getName().equals("img2")) {
                    imageic = new ImageIcon(image.getImage());
                    final OutputStream stream = new FileOutputStream(new File("testing.jpg"));
                    ImageIOUtil.writeImage(image.getImage(), "jpeg", (OutputStream) stream, 75, 1);
                }
            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operador, operands);
        }
    }

    public ImageIcon getImageic() {
        return imageic;
    }

}
