package gtac;

import java.util.Date;

public class Emissao {
    private String cnpj;
    private String nomeTomador;
    private Date dataEmissao;
    private double valor;

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNomeTomador() {
        return nomeTomador;
    }

    public void setNomeTomador(String nomeTomador) {
        this.nomeTomador = nomeTomador;
    }

    public Date getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(Date dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Emissao() {
    }

    public Emissao(String cnpj, String nomeTomador, Date dataEmissao, double valor) {
        this.cnpj = cnpj;
        this.nomeTomador = nomeTomador;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
    }
    
    
    
}
