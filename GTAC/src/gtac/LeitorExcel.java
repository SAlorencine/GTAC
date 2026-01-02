package gtac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.*;

public class LeitorExcel {
    
    private static final String KEY_CNPJ = "cpfcnpjdotomador";
    private static final String KEY_NOME = "razaosocialdotomador";
    private static final String KEY_DATA = "datadofatogerador";
    private static final String KEY_VALOR = "valordosservicos";
    
    public List<Emissao> lerArquivo(File arquivo) throws Exception {
        System.out.println(">>> Iniciando leitura do arquivo: " + arquivo.getName());
        if (arquivo.getName().toLowerCase().endsWith(".csv")) {
            return lerCsv(arquivo);
        } else {
            return lerExcel(arquivo);
        }
    }

    private List<Emissao> lerExcel(File arquivo) {
        List<Emissao> lista = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(arquivo))) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> mapaColunas = mapearColunas(obterCabecalhoExcel(sheet));
            DataFormatter formatter = new DataFormatter();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Emissao emissao = criarEmissaoExcel(row, mapaColunas, formatter);
                if (emissao != null) lista.add(emissao);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private List<Emissao> lerCsv(File arquivo) throws Exception {
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1};
        Exception ultimaExcecao = null;

        for (Charset charset : charsets) {
            try {
                System.out.println("--- Tentando ler CSV com charset: " + charset.displayName() + " ---");
                return tentarLerCsvComCharset(arquivo, charset);
            } catch (Exception e) {
                ultimaExcecao = e;
                System.out.println("Falha com " + charset.displayName() + ": " + e.getMessage());
            }
        }
        throw ultimaExcecao;
    }

    private List<Emissao> tentarLerCsvComCharset(File arquivo, Charset charset) throws Exception {
        List<Emissao> lista = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(arquivo), charset))) {
            String linha = br.readLine();
            if (linha == null) return lista;
            
            linha = linha.replace("\uFEFF", "");
            
            String separador = linha.contains(";") ? ";" : ",";
            System.out.println("Separador detectado: '" + separador + "'");
            
            Map<String, Integer> mapaColunas = mapearColunasCsv(linha, separador);
            System.out.println("Colunas mapeadas com sucesso. Indices: " + mapaColunas);
            
            int numLinha = 1;
            int sucesso = 0;
            
            while ((linha = br.readLine()) != null) {
                numLinha++;
                String[] dados = linha.split(separador); 
                
                
                try {
                    Emissao emissao = criarEmissaoCsv(dados, mapaColunas);
                    if (emissao != null) {
                        lista.add(emissao);
                        sucesso++;
                    } else {
                        
                        System.out.println("Linha " + numLinha + " ignorada (dados inválidos ou vazios).");
                    }
                } catch (Exception e) {
                    System.out.println("Erro na linha " + numLinha + ": " + e.getMessage());
                }
            }
            System.out.println("Leitura concluída. Linhas lidas: " + numLinha + ". Emissões válidas: " + sucesso);
        }
        return lista;
    }

    
    private String normalizar(String texto) {
        if (texto == null) return "";
        String s = Normalizer.normalize(texto, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private Map<String, Integer> mapearColunas(List<String> cabecalho) throws Exception {
        Map<String, Integer> mapa = new HashMap<>();
        for (int i = 0; i < cabecalho.size(); i++) {
            mapa.put(normalizar(cabecalho.get(i)), i);
        }
        
        StringBuilder erros = new StringBuilder();
        if (!mapa.containsKey(KEY_CNPJ)) erros.append("CNPJ (").append(KEY_CNPJ).append("); ");
        if (!mapa.containsKey(KEY_VALOR)) erros.append("Valor (").append(KEY_VALOR).append("); ");
        
        if (erros.length() > 0) {
            throw new Exception("Colunas não encontradas: " + erros.toString() + " | Colunas lidas: " + mapa.keySet());
        }
        return mapa;
    }

    private Map<String, Integer> mapearColunasCsv(String linhaCabecalho, String separador) throws Exception {
        String[] colunas = linhaCabecalho.split(separador);
        List<String> lista = new ArrayList<>();
        for (String s : colunas) {
            lista.add(s.replace("\"", "").trim());
        }
        return mapearColunas(lista);
    }
    
    private List<String> obterCabecalhoExcel(Sheet sheet) {
        List<String> c = new ArrayList<>();
        Row r = sheet.getRow(0);
        if (r != null) for (Cell cell : r) c.add(cell.getStringCellValue());
        return c;
    }


    private Emissao criarEmissaoExcel(Row row, Map<String, Integer> mapa, DataFormatter formatter) {
        return montarEmissao(
            formatter.formatCellValue(row.getCell(mapa.get(KEY_CNPJ))),
            formatter.formatCellValue(row.getCell(mapa.get(KEY_NOME))),
            formatter.formatCellValue(row.getCell(mapa.get(KEY_DATA))),
            formatter.formatCellValue(row.getCell(mapa.get(KEY_VALOR)))
        );
    }

    private Emissao criarEmissaoCsv(String[] dados, Map<String, Integer> mapa) {
        return montarEmissao(
            getDado(dados, mapa.get(KEY_CNPJ)),
            getDado(dados, mapa.get(KEY_NOME)),
            getDado(dados, mapa.get(KEY_DATA)),
            getDado(dados, mapa.get(KEY_VALOR))
        );
    }

    private Emissao montarEmissao(String cnpj, String nome, String dataStr, String valorStr) {
        Emissao emissao = new Emissao();
        emissao.setCnpj(cnpj != null ? cnpj.replaceAll("\\D", "") : "");
        
        if (emissao.getCnpj().isEmpty()) return null;

        emissao.setNomeTomador(nome != null ? nome.replace("\"", "") : "");
        
        if (dataStr != null) {
            String d = dataStr.replace("\"", "").trim();
            if (d.length() > 10) d = d.substring(0, 10); // Corta hora
            try {
                if (d.contains("-")) emissao.setDataEmissao(new SimpleDateFormat("yyyy-MM-dd").parse(d));
                else emissao.setDataEmissao(new SimpleDateFormat("dd/MM/yyyy").parse(d));
            } catch (Exception e) {}
        }

        if (valorStr != null) {
            String v = valorStr.replace("\"", "").trim();
            v = v.replaceAll("[^0-9,.-]", "");
            if (!v.isEmpty()) {
                try {
                    if (v.contains(",") && v.contains(".")) v = v.replace(".", "").replace(",", ".");
                    else if (v.contains(",")) v = v.replace(",", ".");
                    
                    emissao.setValor(Double.parseDouble(v));
                } catch (Exception e) {
                    System.out.println("Erro conv valor: " + valorStr);
                }
            }
        }
        
        return emissao;
    }

    private String getDado(String[] dados, Integer index) {
        if (index == null || index >= dados.length) return "";
        String s = dados[index];
        if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length()-1);
        return s;
    }
}