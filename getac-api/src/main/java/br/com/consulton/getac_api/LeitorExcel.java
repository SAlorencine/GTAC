package br.com.consulton.getac_api;

import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
public class LeitorExcel {
    
    /*
    private = só pode ser acessado nessa classe
    static = não pode ser mudado depois de declarado
    final = pertence a classe e não ao objeto
    */
    private static final String KEY_CNPJ = "cpfcnpjdotomador";
    private static final String KEY_NOME = "razaosocialdotomador";
    private static final String KEY_DATA = "datadofatogerador";
    private static final String KEY_VALOR = "valordosservicos";
    
    /*
    .toLowerCase() => passa tudo para letra minuscula
    .endsWith()  => termina com / pega o final do nome do arquivo
    
    O "if" está verificando se o arquivo termina com .cvs. Caso termine ele retorna a função
    lerCvs, se não retorna lerExcel
    
    */
    
    public List<Emissao> lerArquivo(MultipartFile arquivo) throws Exception {
        String nomeOriginal = arquivo.getOriginalFilename();
        
        if(nomeOriginal != null && nomeOriginal.toLowerCase().endsWith(".csv")){
            return lerCsv(arquivo);
        }else{
            return lerExcel(arquivo.getInputStream());
        }
    }
    
    
    
    private List<Emissao> lerExcel(InputStream InputStream) {
        
        //cria uma lista de objetos Emissão chamada "lista"
        List<Emissao> lista = new ArrayList<>(); 
        
        
        /*
        Workbook é a classe / objeto que representa o arquivo Excel no ApachePOI e que pode ser editado
        WorkbookFactory é responsável por ler o tipo do arquivo (xls / xlsx)
        FileInputStream é responsável por ler a invormação bruta do arquivo importado
        
        
        new FileInputStream(arquivo) => lê o que está em arquivo e passa para um linguagem compreenssivel para o ApachePOI
        WorkbookFactory.create() => lê o FileInputStream, detecta se é xls ou xlsx e cria um Workbook referente ao arquivo
        */
        try (Workbook workbook = WorkbookFactory.create(InputStream)) {
            
            /*
            Sheet representa a página/interface do arquivo
            workbook.getSheetAt(0) => abre o woorbook e lê a aba/página/interface de indice 0, ou seja, a primeira e, nesse caso, única
            */   
            Sheet sheet = workbook.getSheetAt(0);
            
            /*
            Map<chave, valor> => classe de objetos feitos de chave-valor, ou seja, lista chaves com valores específicos ligado a elas
            chave -> String
            valor -> Integer
            
            chama a função mapearColunas() para definir esse Map 
            */
            Map<String, Integer> mapaColunas = mapearColunas(obterCabecalhoExcel(sheet));
            DataFormatter formatter = new DataFormatter();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                /*
                Row => linha
                
                Se a linha for nula o loop (for) continua
                
                */
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
    /*
    Charset[] => codifica caracteres 
    X : Y => para cada Y, um X
    
    */

    private List<Emissao> lerCsv(MultipartFile arquivo) throws Exception {
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1}; // lista de caracteres codificados
        Exception ultimaExcecao = null;

        for (Charset charset : charsets) {
            try {
                System.out.println("--- Tentando ler CSV com charset: " + charset.displayName() + " ---");
                return tentarLerCsvComCharset(arquivo.getInputStream(), charset);
            } catch (Exception e) {
                ultimaExcecao = e;
                System.out.println("Falha com " + charset.displayName() + ": " + e.getMessage());
            }
        }
        throw ultimaExcecao;
    }

    private List<Emissao> tentarLerCsvComCharset(InputStream InputStream, Charset charset) throws Exception {
        List<Emissao> lista = new ArrayList<>();
        /*
        BufferedReader => leitor ágil, guarda temporariamente oque foi lido
        FileInputStream => capta os bytes brutos de um arquivo
        InputStreamReader => decodifica bytes de acordo com um Charset 
        \uFEFF => caracter Unicode invísivel que fica na frente de arquivos codificados
        */
        try (BufferedReader br = new BufferedReader(new InputStreamReader(InputStream, charset))) {
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
        if (texto == null){ return "";}
        /*
        Normalizer => classe de normalização
        .normalize(String, tipo de normalização) => função de normalização
        NFD => decompõe caracteres acentuados ex: "ã" = "a+~"
        
        .replaceAll(X, Y) => trocar todos os carcteres de uma regra X para Y
        \\p{InCombiningDiacriticalMarks} => localiza caracteres q são acentos
        \\p{InCombiningDiacriticalMarks}+ => busca sequencia de acentos
        [] => conjunto de caratere
        ^ => exceto
        a-z => letras do alfabeto
        0-9 => números 
        
        [^a-z0-9] => todo caracter exceto letras de A até Z e números de 0 a 9
        
        */
        String s = Normalizer.normalize(texto, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
    
    
    /*
    A função retornará um Map<> e precisa de uma lista tipo String para funcionar
    
    HashMap<> implmenta a interface do Map em tabelas HASH, ou seja, a monta, mas não pode ser utilizada como Map direto
    
    O for está repetindo até que o num de repetições seja igual ao tamanho da lita.
    A cada repetição i aumenta 1 e representa um objeto de id equivalente na lista.
    A cada repetição o Map<> mapa adiciona uma String de id igual i.
    Toda String adicionada passa pela função normalizar que tira caracteres especiais e deixa tudo em minúsculo
    */
    private Map<String, Integer> mapearColunas(List<String> cabecalho) throws Exception {
        Map<String, Integer> mapa = new HashMap<>();
        for (int i = 0; i < cabecalho.size(); i++) {
            mapa.put(normalizar(cabecalho.get(i)), i);
        }
        
        /*
        StringBuilder => String que permite concatenar Strings mais facilmete.
        .append => chamada para contanar
        .keySet() => Retorna uma vizualização de todas as chaves de um Map
        */
        StringBuilder erros = new StringBuilder();
        if (!mapa.containsKey(KEY_CNPJ)){ erros.append("CNPJ (").append(KEY_CNPJ).append("); ");}
        if (!mapa.containsKey(KEY_VALOR)){ erros.append("Valor (").append(KEY_VALOR).append("); ");}
        
        if (erros.length() > 0) {
            throw new Exception("Colunas não encontradas: " + erros.toString() + " | Colunas lidas: " + mapa.keySet());
        }
        return mapa;
    }

    private Map<String, Integer> mapearColunasCsv(String linhaCabecalho, String separador) throws Exception {
        
        //.split => divide em sub-strings
        String[] colunas = linhaCabecalho.split(separador);
        List<String> lista = new ArrayList<>();
        for (String s : colunas) {
            lista.add(s.replace("\"", "").trim());
        }
        return mapearColunas(lista);
    }
    
    /*
    Função que obtém o cabeçalho do Excel
    Cell => célula da tabela
    .getStringCellValue() => pega o valor da célula em formato de String
    */
    private List<String> obterCabecalhoExcel(Sheet sheet) {
        List<String> c = new ArrayList<>();
        Row r = sheet.getRow(0);
        if (r != null) for (Cell cell : r) c.add(cell.getStringCellValue());
        return c;
    }
    
    /*
    formate.formatCellValue => transforma o valor de uma célula para String
    */
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

    /*
    \\D => qualquer caracter não numérico
    \" => aspas dupla
    substring(X,Y) => divide a string do índice X até Y. Caso Y esteja oculto é até o final
    .trim() => remove espaços em branco e quebras
    SimpleDateFormat("dd/MM/yyyy").parse(d) => passa a string para um modelo de data específico -> de String para Data
    */
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