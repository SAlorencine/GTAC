package br.com.consulton.getac_api;

import br.com.consulton.getac_api.service.ComparadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("api/gtac")
@CrossOrigin(origins = "*")
public class GtacController { 
    
     
    @Autowired
    private ComparadorService comparadorService;
    
    @Autowired
    private GeradorPDF geradorPDF;
    
    @PostMapping(value = "/analisar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> processarPlanilha(@RequestParam("arquivoMes1") MultipartFile arquivoMes1,
            @RequestParam("arquivoMes2") MultipartFile arquivoMes2,
            @RequestParam("nomeMes1") String nomeMes1,
            @RequestParam("nomeMes2") String nomeMes2){
    
        
        try{
            LeitorExcel leitorExcel = new LeitorExcel();
            List<Emissao> listaMes1 = leitorExcel.lerArquivo(arquivoMes1);
            List<Emissao> listaMes2 = leitorExcel.lerArquivo(arquivoMes2);
            
            List<ResultadoAnalise> resultados = comparadorService.compararArquivos(listaMes1, listaMes2, nomeMes1, nomeMes2);
            
            byte[] pdfFinal = geradorPDF.gerarRelatorio(resultados, nomeMes1, nomeMes2);
            
            
            // Se algo for vazio
            if (pdfFinal == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }



            if(arquivoMes1.isEmpty() || arquivoMes2.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
                       
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            headers.setContentDispositionFormData("attachment", "Relatorio_Divergencias.pdf");
            
            return new ResponseEntity<>(pdfFinal, headers, HttpStatus.OK );
            
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    
    }
    
}
