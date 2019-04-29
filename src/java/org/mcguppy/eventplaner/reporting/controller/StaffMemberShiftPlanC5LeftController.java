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
import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
@Named
@RequestScoped
public class StaffMemberShiftPlanC5LeftController {

    public StaffMemberShiftPlanC5LeftController() {
        facesContext = FacesContext.getCurrentInstance();
        jpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
        dateString = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        dateStringHuman = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
    }
    private StaffMemberJpaController jpaController = null;
    private FacesContext facesContext = null;
    private String dateString;
    private String dateStringHuman;
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
    private static final Font tableHeadFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    private static final Font smallNormal = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    private static final Font addressNormal = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);


    // TODO: exception handling
    public String createShiftPlanForStaffmembersC5left() throws DocumentException, FileNotFoundException, IOException {

        Document document = new Document();
        document.setPageSize(PageSize.A4);

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"personal_schichtplan_c5_links" + dateString + ".pdf\"");
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        addMetaData(document);
        addContent(document);
        document.close();

        facesContext.responseComplete();
        return "schiftPlanCreated";
    }

    private void addMetaData(Document document) {
        document.addTitle("Personal-Schichtplan C5 links " + dateString);
        document.addSubject("Personal-Schichtplan " + dateString + " für den Event C5 links");
        document.addKeywords("Event, PDF, Plan, Personal, Helfer, Staff, C5, links");
        document.addAuthor("Stefan Meichtry");
        document.addCreator("Stefan Meichtry");
    }

    private void addContent(Document document) throws DocumentException {

        List<StaffMember> staffMembers = jpaController.findStaffMemberEntities();
        Collections.sort(staffMembers);
        PdfPTable staffMemberTable = null;
        for (StaffMember staffMember : staffMembers) {
            if (staffMember.getShifts().isEmpty()) {
                continue;
            }

            // preface
            Paragraph preface = new Paragraph();
            addEmptyLine(preface, 1);
            addEmptyLine(preface, 1);
            addEmptyLine(preface, 1);
            addEmptyLine(preface, 1);
            addEmptyLine(preface, 1);
            addEmptyLine(preface, 1);
            addEmptyLine(preface, 1);
            document.add(preface);

            // staffMember Data
            staffMemberTable = new PdfPTable(new float[]{100});
            staffMemberTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            staffMemberTable.addCell(new Paragraph(staffMember.getTitle().toString(), addressNormal));
            staffMemberTable.addCell(new Paragraph((staffMember.getFirstName() + ' ' + staffMember.getLastName()), addressNormal));
            staffMemberTable.addCell(new Paragraph(staffMember.getStreet(), addressNormal));
            staffMemberTable.addCell(new Paragraph((staffMember.getZip() + ' ' + staffMember.getCity()), addressNormal));

            document.add(staffMemberTable);

            // shift Data
            Paragraph shiftDataSection = new Paragraph();
            addEmptyLine(shiftDataSection, 1);
            addEmptyLine(shiftDataSection, 1);
            addEmptyLine(shiftDataSection, 1);
            addEmptyLine(shiftDataSection, 1);
            addEmptyLine(shiftDataSection, 1);
            addEmptyLine(shiftDataSection, 1);
            shiftDataSection.add(new Paragraph("Schicht-Daten:", subFont));
            addEmptyLine(shiftDataSection, 1);
            document.add(shiftDataSection);

            PdfPTable shiftTable = new PdfPTable(new float[]{22, 32, 17, 17, 20});

            PdfPCell c1 = new PdfPCell(new Phrase("Standort", tableHeadFont));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("Beschreibung", tableHeadFont));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Start", tableHeadFont));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase("Ende", tableHeadFont));
            c4.setHorizontalAlignment(Element.ALIGN_CENTER);
            c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c4);

            PdfPCell c5 = new PdfPCell(new Phrase("Schicht-Verantwortung", tableHeadFont));
            c5.setHorizontalAlignment(Element.ALIGN_CENTER);
            c5.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c5);

            List<Shift> shifts = (List<Shift>) staffMember.getShifts();
            Collections.sort(shifts);

            for (Shift shift : shifts) {
                shiftTable.addCell(new Paragraph(shift.getLocation().getLocationName(), smallNormal));
                shiftTable.addCell(new Paragraph(shift.getLocation().getDescription(), smallNormal));

                SimpleDateFormat formatter = new SimpleDateFormat("E. dd.MM.yyyy HH:mm", Locale.GERMAN);

                Date startTime = shift.getStartTime();
                Date endTime = shift.getEndTime();

                String startTimeString = formatter.format(startTime);
                String endTimeString = formatter.format(endTime);

                shiftTable.addCell(new Paragraph(startTimeString, smallNormal));
                shiftTable.addCell(new Paragraph(endTimeString, smallNormal));
                if (shift.getResponsible() == null) {
                    shiftTable.addCell(new Paragraph("", smallNormal));
                } else {
                    shiftTable.addCell(new Paragraph(shift.getResponsible().toString(), smallNormal));
                }
            }

            document.add(shiftTable);

            document.newPage();

        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
