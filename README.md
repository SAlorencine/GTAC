# GTAC - Gerenciador de Tabelas e Arquivos Contábeis

> Sistema em Java desenvolvido para automatizar a conferência financeira de Notas Fiscais (Nota Paulistana), identificando divergências de valores entre meses e gerando relatórios analíticos.

## 📌 Sobre o Projeto

O **GTAC** é uma aplicação Desktop que visa facilitar a conciliação financeira de serviços tomados. Ele lê arquivos exportados do sistema da Nota Fiscal Paulistana (suportando tanto CSV quanto Excel), agrupa os valores por CNPJ e compara os totais entre dois meses distintos (ex: Mês Anterior vs. Mês Atual).

O resultado é exportado automaticamente para um **Relatório em PDF**, detalhando:
- CNPJs analisados.
- Divergências de valores (para mais ou para menos).
- Detalhamento das notas fiscais envolvidas.

## 🚀 Funcionalidades

- **Leitura Híbrida:** Suporte para importação de arquivos `.csv` e `.xls/.xlsx` (Excel).
- **Processamento Inteligente:** Agrupamento automático de emissões por CNPJ do tomador.
- **Análise Comparativa:**
  - Identifica notas presentes em um mês e ausentes no outro.
  - Calcula a diferença exata de valores entre os períodos.
- **Geração de Relatórios:** Criação automática de arquivos PDF contendo o "Relatório de Divergências" ou "Relatório Sem Divergências".
- **Interface Gráfica (GUI):** Interface amigável construída com Java Swing para seleção de arquivos e meses.

## 🛠 Tecnologias Utilizadas

O projeto foi desenvolvido utilizando:

- **Linguagem:** [Java](https://www.java.com/)
- **Interface Gráfica:** Java Swing (JFrame)
- **Manipulação de Arquivos (Office/CSV):** [Apache POI](https://poi.apache.org/)
- **Geração de PDF:** [Apache PDFBox](https://pdfbox.apache.org/)
- **IDE:** NetBeans

## 📦 Como Usar

1. Execute o arquivo `.jar` ou a classe principal `GTAC.java` na sua IDE.
2. Na tela inicial:
   - Selecione o arquivo referente ao **Mês Anterior** (botão "Selecionar").
   - Escolha o número do mês correspondente no menu.
   - Selecione o arquivo referente ao **Mês Presente**.
   - Escolha o número do mês correspondente.
3. Clique em **"Continuar"**.
4. O sistema processará os dados e exibirá uma mensagem de confirmação com o caminho do PDF gerado (ex: `Relatorio_Analise_01_02.pdf`).

## 📋 Estrutura dos Arquivos de Entrada

Para que o sistema leia corretamente, os arquivos de entrada (CSV ou Excel) devem conter colunas com cabeçalhos similares a:
- `CPF/CNPJ do Tomador`
- `Razão Social do Tomador`
- `Data do Fato Gerador`
- `Valor dos Serviços`

## 🔧 Instalação e Build

### Pré-requisitos
- Java JDK instalado (versão recomendada: 17 ou superior).

### Compilando o projeto
Este é um projeto **NetBeans**. Para compilar:
1. Abra o projeto no NetBeans.
2. Certifique-se de que as bibliotecas (Apache POI, PDFBox, Commons IO) estão configuradas no Classpath.
3. Execute "Clean and Build".

---
Desenvolvido para otimizar processos de análise fiscal.
