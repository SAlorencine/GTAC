
# GTAC Cloud - Análise Comparativa 📊

O GTAC (Gerenciador de Tarefas e Análise Comparativa) é uma aplicação web *stateless* desenvolvida para realizar a leitura, comparação e geração de relatórios de divergências entre planilhas de dados (Excel/CSV).

## 🚀 Funcionalidades

*   **Upload de Arquivos:** Interface amigável para envio de duas planilhas de meses distintos.
*   **Processamento em Memória:** Leitura e extração de dados (Apache POI) sem necessidade de armazenamento persistente.
*   **Motor de Comparação:** Algoritmo que cruza os dados das emissões e identifica divergências.
*   **Geração de Relatórios:** Exportação automática dos resultados compilados em um documento PDF (Apache PDFBox).

## 🛠️ Tecnologias Utilizadas

*   **Backend:** Java 21+ | Spring Boot 3.x
*   **Manipulação de Excel:** Apache POI
*   **Geração de PDF:** Apache PDFBox
*   **Frontend:** HTML5 | Vanilla JS | Tailwind CSS
*   **Build Tool:** Maven

## 📁 Arquitetura do Projeto

A aplicação segue o padrão de separação de responsabilidades (MVC/Service Pattern):
*   `Controllers`: Gerenciamento das requisições HTTP e roteamento.
*   `Services`: Lógica de negócios e regras de comparação.
*   `Utils`: Componentes especialistas (`LeitorExcel`, `GeradorPDF`).
*   `Static`: Arquivos de frontend servidos nativamente pelo Tomcat embutido.

## ⚙️ Como Executar Localmente

**Pré-requisitos:**
*   JDK 21 ou superior instalado.
*   Maven 3.8+ (Opcional, o projeto inclui o Maven Wrapper).
