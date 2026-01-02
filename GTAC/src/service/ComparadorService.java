package service;

import gtac.Emissao;
import gtac.ResultadoAnalise;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComparadorService {
    
    public List<ResultadoAnalise> compararArquivos(List<Emissao> emissoes1, List<Emissao> emissoes2, String nomeMes1, String nomeMes2) {
        List<ResultadoAnalise> resultados = new ArrayList<>();

        Map<String, List<Emissao>> mapaMes1 = agruparPorCnpj(emissoes1);
        Map<String, List<Emissao>> mapaMes2 = agruparPorCnpj(emissoes2);

        Set<String> todosCnpjs = new HashSet<>();
        todosCnpjs.addAll(mapaMes1.keySet());
        todosCnpjs.addAll(mapaMes2.keySet());

        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (String cnpj : todosCnpjs) {
            List<Emissao> lista1 = mapaMes1.getOrDefault(cnpj, new ArrayList<>());
            List<Emissao> lista2 = mapaMes2.getOrDefault(cnpj, new ArrayList<>());

            // Calcula totais
            double somaMes1 = lista1.stream().mapToDouble(Emissao::getValor).sum();
            double somaMes2 = lista2.stream().mapToDouble(Emissao::getValor).sum();

            Comparator<Emissao> comparadorData = Comparator.comparing(Emissao::getDataEmissao, Comparator.nullsLast(Comparator.naturalOrder()));
            Collections.sort(lista1, comparadorData);
            Collections.sort(lista2, comparadorData);

            String nomeTomador = "DESCONHECIDO";
            if (!lista1.isEmpty()) nomeTomador = lista1.get(0).getNomeTomador();
            else if (!lista2.isEmpty()) nomeTomador = lista2.get(0).getNomeTomador();

            ResultadoAnalise resultado = new ResultadoAnalise(cnpj, nomeTomador);
            resultado.setListaMes1(lista1);
            resultado.setListaMes2(lista2);
            resultado.setTotalMes1(somaMes1); 
            resultado.setTotalMes2(somaMes2); 

            int maxLinhas = Math.max(lista1.size(), lista2.size());
            boolean temDivergencia = false;

            for (int i = 0; i < maxLinhas; i++) {
                double val1 = (i < lista1.size()) ? lista1.get(i).getValor() : 0.0;
                double val2 = (i < lista2.size()) ? lista2.get(i).getValor() : 0.0;
                double diferenca = val1 - val2;

                if (i >= lista1.size() && i < lista2.size()) {
                    resultado.addFrase("Não há emissão correspondente ao mês anterior (" + nomeMes1 + ");");
                    temDivergencia = true;

                } else if (i < lista1.size() && i >= lista2.size()) {
                    resultado.addFrase("O Mês " + nomeMes1 + " tem nota de R$ " + df.format(val1) + " sem correspondência em " + nomeMes2 + ";");
                    temDivergencia = true;

                } else {
                    if (Math.abs(diferenca) < 0.01) {
                        resultado.addFrase("Valores coincidem;");
                    } else if (diferenca > 0) {
                        resultado.addFrase("Mês " + nomeMes1 + " tem R$ " + df.format(diferenca) + " a MAIS que " + nomeMes2 + ";");
                        temDivergencia = true;
                    } else {
                        resultado.addFrase("Mês " + nomeMes1 + " tem R$ " + df.format(Math.abs(diferenca)) + " a MENOS que " + nomeMes2 + ";");
                        temDivergencia = true;
                    }
                }   
            }
            
            if (temDivergencia) {
                resultados.add(resultado); 
            }
        } 
        
        return resultados;
    }    

    private Map<String, List<Emissao>> agruparPorCnpj(List<Emissao> lista) {
        Map<String, List<Emissao>> mapa = new HashMap<>();
        for (Emissao e : lista) {
            if (e.getCnpj() == null || e.getCnpj().isEmpty()) continue;
            String key = e.getCnpj().trim().replaceAll("\\D", ""); 
            mapa.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }
        return mapa;
    }
}