package app.di_v.etc.blockchain.controller;

import app.di_v.etc.blockchain.model.Block;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Blockchain Controller
 * @author di-v
 */
@Controller
public class Blockchain {
    private byte[] bytes;
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 2;

    @RequestMapping(value="/")
    public String index() {
        return "index";
    }

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody String blockUpload(@RequestParam("file") MultipartFile file){
        if(isChainValid()) {
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File( "uploaded-file")));
                    stream.write(bytes);
                    stream.close();

                    if (blockchain.size() != 0) {
                        blockchain.add(new Block(bytes, blockchain.get(blockchain.size()-1).getHash()));
                        System.out.println("Trying to Mine block ... ");
                        blockchain.get(blockchain.size()-1).mineBlock(difficulty);
                    } else {
                        blockchain.add(new Block(bytes, "0"));
                        System.out.println("Trying to init mine block ... ");
                        blockchain.get(0).mineBlock(difficulty);
                    }

                    return "Успешно загружено. Хэш: " + blockchain.get(blockchain.size()-1).getHash();
                } catch (Exception e) {
                    return "Не удалось загрузить: " + e.getMessage();
                }
            } else {
                return "Не удалось загрузить пустой файл.";
            }
        } else return "Цепочка блоков разрушена :(";

    }

    /**
     * Загрузка с сервлета файла
     * @param name
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getBlock", method = RequestMethod.GET, produces = "application/png")
    public @ResponseBody HttpEntity<byte[]> downloadB(@RequestParam(value="name", required=false, defaultValue="0") String name) throws IOException {
        System.out.println("Запрос на загрузку с сервлета данных. Значение хэша= " + name);

        int index = 0;
        System.out.println("Состояние Блокчейна");
        for (int i = 0; i < blockchain.size(); i++) {
            int n = blockchain.get(i).getHash().indexOf(name);
            if (n != -1){
                index = i;
            }
            System.out.println("Блок"+ i + ": { \n Хэш= "  + blockchain.get(i).getHash() +
                    "\n Данные= " + blockchain.get(i).getData() +
                    "\n Дата= " + blockchain.get(i).getTimeStamp() +
                    "\n Хэш предыдущего блока= " + blockchain.get(i).getPreviousHash());
        }
        System.out.println("index: " + index);

        Block block = blockchain.get(index);
        byte[] document = block.getData();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "png"));
        header.set("Content-Disposition", "inline; filename=" + name + ".png");
        return new HttpEntity<byte[]>(document, header);
    }


    /**
     * метод isChainValid() перебирает все блоки в цепочке и сравнивать хэши.
     * (переменная hash равна вычисленному хэшу,в а хэш предыдущего блока равен переменной previousHash).
     * @return
     */
    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }
}
