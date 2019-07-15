/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfhandleapp;

import javax.swing.ImageIcon;

/**
 *
 * @author MX00494-EXT
 */
public class CodeTokenModel {

    String Token;
    ImageIcon imageToken;

    public CodeTokenModel(String Token, ImageIcon imageToken) {
        this.Token = Token;
        this.imageToken = imageToken;
    }

    public void setImageToken(ImageIcon imageToken) {
        this.imageToken = imageToken;
    }

    public void setToken(String Token) {
        this.Token = Token;
    }

    public ImageIcon getImageToken() {
        return imageToken;
    }

    public String getToken() {
        return Token;
    }

}
