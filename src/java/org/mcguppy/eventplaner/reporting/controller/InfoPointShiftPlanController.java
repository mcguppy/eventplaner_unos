package org.mcguppy.eventplaner.reporting.controller;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.mcguppy.eventplaner.jpa.controllers.ShiftJpaController;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
@Named
@RequestScoped
public class InfoPointShiftPlanController {

    public InfoPointShiftPlanController() {
        facesContext = FacesContext.getCurrentInstance();
        jpaController = (ShiftJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "shiftJpa");
        dateString = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        dateStringHuman = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
    }
    private ShiftJpaController jpaController = null;
    private FacesContext facesContext = null;
    private String dateString;
    private String dateStringHuman;
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
    private static final Font tableHeadFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    private static final Font smallNormal = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

    // TODO: exception handling
    public String createShiftPlanForInfoPoint() throws DocumentException, FileNotFoundException, IOException {

        Document document = new Document();
        document.setPageSize(PageSize.A4.rotate());     // landscape format

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"info_point_schichtplan_" + dateString + ".pdf\"");
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        addMetaData(document);
        addContent(document);
        document.close();

        facesContext.responseComplete();
        return "schiftPlanCreated";
    }

    private void addMetaData(Document document) {
        document.addTitle("Info-Point-Schichtplan " + dateString);
        document.addSubject("Info-Point-Schichtplan " + dateString + " für den Event");
        document.addKeywords("Event, PDF, Plan, Info-Point");
        document.addAuthor("Stefan Meichtry");
        document.addCreator("Stefan Meichtry");
    }

    private void addContent(Document document) throws DocumentException {

        List<Shift> myShifts = jpaController.findShiftEntities();
        Collections.sort(myShifts);
        PdfPTable myShiftTable = null;
        for (Shift shift : myShifts) {
            if (shift.getStaffMembers().isEmpty()) {
                continue;
            }

            // preface
            Paragraph preface = new Paragraph();
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("eventplaner", catFont));
            addEmptyLine(preface, 1);
            document.add(preface);

            // shift Data
            Paragraph myShiftDataSection = new Paragraph();
            addEmptyLine(myShiftDataSection, 1);
            myShiftDataSection.add(new Paragraph("Schicht-Detail:", subFont));
            addEmptyLine(myShiftDataSection, 1);
            document.add(myShiftDataSection);

            myShiftTable = new PdfPTable(new float[]{15, 40});
            myShiftTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            SimpleDateFormat formatter = new SimpleDateFormat("E. dd.MM.yyyy HH:mm", Locale.GERMAN);

            Date startTime = shift.getStartTime();
            Date endTime = shift.getEndTime();

            String startTimeString = formatter.format(startTime);
            String endTimeString = formatter.format(endTime);

            myShiftTable.addCell(new Paragraph("Standort:", smallNormal));
            myShiftTable.addCell(new Paragraph(shift.getLocation().getLocationName(), smallNormal));
            myShiftTable.addCell(new Paragraph("Beschreibung:", smallNormal));
            myShiftTable.addCell(new Paragraph(shift.getLocation().getDescription(), smallNormal));
            myShiftTable.addCell(new Paragraph("Start:", smallNormal));
            myShiftTable.addCell(new Paragraph(startTimeString, smallNormal));
            myShiftTable.addCell(new Paragraph("Ende:", smallNormal));
            myShiftTable.addCell(new Paragraph(endTimeString, smallNormal));
            myShiftTable.addCell(new Paragraph("Personen-Bedarf:", smallNormal));
            myShiftTable.addCell(new Paragraph(Integer.valueOf(shift.getNumberOfStaffMembers()).toString(), smallNormal));
            myShiftTable.addCell(new Paragraph("Schicht-Verantwortung:", smallNormal));
            if (shift.getResponsible() == null) {
                myShiftTable.addCell(new Paragraph("", smallNormal));
            } else {
                myShiftTable.addCell(new Paragraph(shift.getResponsible().toString(), smallNormal));
            }

            document.add(myShiftTable);

            // staffMember Data
            Paragraph staffMemberDataSection = new Paragraph();
            addEmptyLine(staffMemberDataSection, 1);
            staffMemberDataSection.add(new Paragraph("Schicht-Personen:", subFont));
            addEmptyLine(staffMemberDataSection, 1);
            document.add(staffMemberDataSection);

            PdfPTable staffMemberTable = new PdfPTable(new float[]{10, 17, 15, 9, 26, 9, 17, 22, 20});

            PdfPCell c1 = new PdfPCell(new Phrase("Anrede", tableHeadFont));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("Nachname", tableHeadFont));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Vorname", tableHeadFont));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase("T-Shirt", tableHeadFont));
            c4.setHorizontalAlignment(Element.ALIGN_CENTER);
            c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c4);

            PdfPCell c5 = new PdfPCell(new Phrase("Strasse", tableHeadFont));
            c5.setHorizontalAlignment(Element.ALIGN_CENTER);
            c5.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c5);

            PdfPCell c6 = new PdfPCell(new Phrase("PLZ", tableHeadFont));
            c6.setHorizontalAlignment(Element.ALIGN_CENTER);
            c6.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c6);

            PdfPCell c7 = new PdfPCell(new Phrase("Ort", tableHeadFont));
            c7.setHorizontalAlignment(Element.ALIGN_CENTER);
            c7.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c7);

            PdfPCell c8 = new PdfPCell(new Phrase("Telefon Nummer", tableHeadFont));
            c8.setHorizontalAlignment(Element.ALIGN_CENTER);
            c8.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c8);

            PdfPCell c9 = new PdfPCell(new Phrase("Natel Nummer", tableHeadFont));
            c9.setHorizontalAlignment(Element.ALIGN_CENTER);
            c9.setBackgroundColor(BaseColor.LIGHT_GRAY);
            staffMemberTable.addCell(c9);

            List<StaffMember> staffMembers = (List<StaffMember>) shift.getStaffMembers();
            Collections.sort(staffMembers);

            for (StaffMember staffMemeber : shift.getStaffMembers()) {
                staffMemberTable.addCell(new Paragraph(staffMemeber.getTitle().toString(), smallNormal));
                staffMemberTable.addCell(new Paragraph(staffMemeber.getLastName(), smallNormal));
                staffMemberTable.addCell(new Paragraph(staffMemeber.getFirstName(), smallNormal));
                if (null != staffMemeber.getShirt()) {
                    staffMemberTable.addCell(new Paragraph(staffMemeber.getShirt().toString(), smallNormal));
                } else {
                    staffMemberTable.addCell(new Paragraph("", smallNormal));
                }
                staffMemberTable.addCell(new Paragraph(staffMemeber.getStreet(), smallNormal));
                staffMemberTable.addCell(new Paragraph(staffMemeber.getZip(), smallNormal));
                staffMemberTable.addCell(new Paragraph(staffMemeber.getCity(), smallNormal));
                staffMemberTable.addCell(new Paragraph(staffMemeber.getPhoneNr(), smallNormal));
                staffMemberTable.addCell(new Paragraph(staffMemeber.getCellPhoneNr(), smallNormal));
            }

            document.add(staffMemberTable);

            document.newPage();

        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
