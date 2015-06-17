package com.itextpdf.core.pdf.formfield;

import com.itextpdf.core.pdf.*;
import com.itextpdf.core.pdf.action.PdfAction;
import com.itextpdf.core.pdf.annot.PdfAnnotation;
import com.itextpdf.core.pdf.annot.PdfWidgetAnnotation;


public abstract class PdfFormField extends PdfObjectWrapper<PdfDictionary> {

    protected PdfWidgetAnnotation widget;

    protected PdfFormField(PdfDictionary pdfObject) {
        super(pdfObject);
    }

    protected PdfFormField(PdfDictionary pdfObject, PdfDocument pdfDocument) {
        super(pdfObject, pdfDocument);
    }

    public PdfFormField(PdfDocument pdfDocument, PdfWidgetAnnotation widget) {
        this(new PdfDictionary(), pdfDocument);
        this.widget = widget;
        put(PdfName.FT, getFormType());
    }

    public static <T extends PdfFormField> T makeFormField(PdfObject pdfObject, PdfDocument document){
        T field = null;
        if(pdfObject.isIndirectReference())
            pdfObject = ((PdfIndirectReference)pdfObject).getRefersTo();
        if(pdfObject.isDictionary()){
            PdfDictionary dictionary = (PdfDictionary) pdfObject;
            PdfName formType = dictionary.getAsName(PdfName.FT);
            if(PdfName.Tx.equals(formType))
                field = (T) new PdfTextFormField(dictionary, document);
            else if(PdfName.Btn.equals(formType))
                field = (T) new PdfButtonFormField(dictionary, document);
            else if(PdfName.Ch.equals(formType))
                field = (T) new PdfChoiceFormField(dictionary, document);
            else if(PdfName.Sig.equals(formType))
                field = (T) new PdfSignatureFormField(dictionary, document);
            else
                throw new UnsupportedOperationException();
            if (dictionary.get(PdfName.Subtype) != null && dictionary.get(PdfName.Subtype).equals(PdfName.Widget)){
                field.widget = PdfAnnotation.makeAnnotation(dictionary, document);
            }
        }

        return field;
    }

    public abstract PdfName getFormType();

    public abstract <T extends PdfFormField> T setValue(PdfObject value);

    public PdfFormField setParent(PdfFormField parent){
        return put(PdfName.Parent, parent);
    }

    public PdfDictionary getParent(){
        return getPdfObject().getAsDictionary(PdfName.Parent);
    }

    public PdfArray getKids(){
        return getPdfObject().getAsArray(PdfName.Kids);
    }

    public PdfFormField addKid (PdfFormField kid){
        kid.setParent(this);
        PdfArray kids = getKids();
        if (kids == null){
            kids = new PdfArray();
        }
        kids.add(kid.getPdfObject());
        return put(PdfName.Kids, kids);
    }

    public PdfFormField addKid (PdfWidgetAnnotation kid){
        kid.setParent(getPdfObject());
        PdfArray kids = getKids();
        if (kids == null){
            kids = new PdfArray();
        }
        kids.add(kid.getPdfObject());
        return put(PdfName.Kids, kids);
    }

    public PdfFormField setFieldName(String name){
        return put(PdfName.T, new PdfString(name));
    }

    public PdfString getFieldName(){
        return getPdfObject().getAsString(PdfName.T);
    }

    public PdfFormField setAlternativeName(String name){
        return put(PdfName.TU, new PdfString(name));
    }

    public PdfString getAlternativeName(){
        return getPdfObject().getAsString(PdfName.TU);
    }

    public PdfFormField setMappingName(String name){
        return put(PdfName.TM, new PdfString(name));
    }

    public PdfString getMappingName(){
        return getPdfObject().getAsString(PdfName.TM);
    }

    public PdfFormField setFieldFlags(int flags){
        return put(PdfName.Ff, new PdfNumber(flags));
    }

    public PdfFormField setFieldFlag(int flag){
        int flags = getFieldFlags();
        flags = flags | flag;

        return setFieldFlags(flags);
    }

    public int getFieldFlags(){
        PdfNumber f = getPdfObject().getAsNumber(PdfName.Ff);
        if (f != null)
            return f.getIntValue();
        else
            return 0;
    }

    public PdfObject getValue(){
        return getPdfObject().get(PdfName.V);
    }

    public PdfFormField setDefaultValue(PdfObject value){
        return put(PdfName.DV, value);
    }

    public PdfObject getDefaultValue(){
        return getPdfObject().get(PdfName.DV);
    }

    public PdfFormField setAdditionalAction(PdfAction action){
        return put(PdfName.AA, action);
    }

    public PdfDictionary getAdditionalAction(){
        return getPdfObject().getAsDictionary(PdfName.AA);
    }

    public PdfWidgetAnnotation getWidget(){
        return widget;
    }
}