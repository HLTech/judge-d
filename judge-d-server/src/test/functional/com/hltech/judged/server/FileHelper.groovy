package com.hltech.judged.server

class FileHelper {

    static loadFromFileAndFormat(String filePath) {
        new File("src/test/resources/$filePath").text.replaceAll('\n','').replaceAll('\r','')
    }
}
