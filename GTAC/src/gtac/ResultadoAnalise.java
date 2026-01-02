package gtac;

import java.util.ArrayList;
import java.util.List;

public class ResultadoAnalise {
    private String cnpj;
    private String nome;
    private double totalMes1; 
    private double totalMes2; 
    private List<Emissao> listaMes1 = new ArrayList<>();
    private List<Emissao> listaMes2 = new ArrayList<>();
    private List<String> frasesResultado = new ArrayList<>();

    public ResultadoAnalise() {
    }

    public ResultadoAnalise(String cnpj, String nome) {
        this.cnpj = cnpj;
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getTotalMes1() {
        return totalMes1;
    }

    public void setTotalMes1(double totalMes1) {
        this.totalMes1 = totalMes1;
    }

    public double getTotalMes2() {
        return totalMes2;
    }

    public void setTotalMes2(double totalMes2) {
        this.totalMes2 = totalMes2;
    }

    public List<Emissao> getListaMes1() {
        return listaMes1;
    }

    public void setListaMes1(List<Emissao> listaMes1) {
        this.listaMes1 = listaMes1;
    }

    public List<Emissao> getListaMes2() {
        return listaMes2;
    }

    public void setListaMes2(List<Emissao> listaMes2) {
        this.listaMes2 = listaMes2;
    }

    public List<String> getFrasesResultado() {
        return frasesResultado;
    }

    public void setFrasesResultado(List<String> frasesResultado) {
        this.frasesResultado = frasesResultado;
    }

    public void addFrase(String frase) {
        if (this.frasesResultado == null) {
            this.frasesResultado = new ArrayList<>();
        }
        this.frasesResultado.add(frase);
    }

    public List<String> getFrases() {
        return frasesResultado;
    }
}