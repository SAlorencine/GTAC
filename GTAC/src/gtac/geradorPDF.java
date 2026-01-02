package gtac;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts; 

public class geradorPDF {

    public void gerarRelatorio(List<ResultadoAnalise> resultados, String caminhoSaida, String mes1, String mes2) {
        try (PDDocument document = new PDDocument()) {
            
            if (resultados.isEmpty()) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText("Relatório de Análise - Sem Divergências");
                    contentStream.endText();

                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 650);
                    contentStream.showText("Não foram encontradas diferenças de valores entre os meses " + mes1 + " e " + mes2 + ".");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 630);
                    contentStream.showText("Todos os CNPJs analisados apresentam valores coincidentes.");
                    contentStream.endText();
                }
                document.save(new File(caminhoSaida));
                return; 
            }

            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            
            DecimalFormat df = new DecimalFormat("#,##0.00");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

            float yPosition = 750;
            float margin = 50;
            float linhaAltura = 12; 
            float linhaSeparacao = 15; 

            contentStream.setFont(fontBold, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Relatório de Divergências (" + mes1 + " vs " + mes2 + ")");
            contentStream.endText();
            yPosition -= 40;

            for (ResultadoAnalise resultado : resultados) {
                
                if (yPosition < 100) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                contentStream.setFont(fontBold, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("CNPJ: " + resultado.getCnpj() + " - " + resultado.getNome());
                contentStream.endText();
                yPosition -= linhaSeparacao;

                if (yPosition < 100) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                double dif = resultado.getTotalMes1() - resultado.getTotalMes2();
                contentStream.setFont(fontRegular, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Valor Mês " + mes1 + ": R$ " + df.format(resultado.getTotalMes1()) + 
                                       "   |   Valor Mês " + mes2 + ": R$ " + df.format(resultado.getTotalMes2()) +
                                       "   |   Diferença Total: R$ " + df.format(dif));
                contentStream.endText();
                yPosition -= linhaSeparacao;

                if (!resultado.getListaMes1().isEmpty()) {
                    if (yPosition < 100) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                    contentStream.setFont(fontBold, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Mês " + mes1 + ":");
                    contentStream.endText();
                    yPosition -= linhaAltura;

                    contentStream.setFont(fontRegular, 10);
                    for (Emissao e : resultado.getListaMes1()) {
                        if (yPosition < 80) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 10, yPosition);
                        String dataStr = (e.getDataEmissao() != null) ? sdf.format(e.getDataEmissao()) : "--/--";
                        contentStream.showText("- R$ " + df.format(e.getValor()) + "  (" + dataStr + ")");
                        contentStream.endText();
                        yPosition -= linhaAltura;
                    }
                    yPosition -= 5; 
                }

                if (!resultado.getListaMes2().isEmpty()) {
                    if (yPosition < 100) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                    contentStream.setFont(fontBold, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Mês " + mes2 + ":");
                    contentStream.endText();
                    yPosition -= linhaAltura;

                    contentStream.setFont(fontRegular, 10);
                    for (Emissao e : resultado.getListaMes2()) {
                        if (yPosition < 80) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 10, yPosition);
                        String dataStr = (e.getDataEmissao() != null) ? sdf.format(e.getDataEmissao()) : "--/--";
                        contentStream.showText("- R$ " + df.format(e.getValor()) + "  (" + dataStr + ")");
                        contentStream.endText();
                        yPosition -= linhaAltura;
                    }
                    yPosition -= 5;
                }

                if (yPosition < 100) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                contentStream.setFont(fontBold, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Resultado:");
                contentStream.endText();
                yPosition -= linhaAltura;

                contentStream.setFont(fontRegular, 10);
                for (String frase : resultado.getFrases()) {
                    if (frase.contains("Valores coincidem")) continue; 
                    
                    if (yPosition < 80) { contentStream = novaPagina(document, contentStream); yPosition = 750; }
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 10, yPosition);
                    contentStream.showText("- " + frase);
                    contentStream.endText();
                    yPosition -= linhaAltura;
                }
                
                yPosition -= 25; 
                
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(margin, yPosition + 15);
                contentStream.lineTo(550, yPosition + 15);
                contentStream.stroke();
            }

            contentStream.close();
            document.save(new File(caminhoSaida));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PDPageContentStream novaPagina(PDDocument doc, PDPageContentStream currentStream) throws IOException {
        currentStream.close();
        PDPage page = new PDPage();
        doc.addPage(page);
        return new PDPageContentStream(doc, page);
    }
}