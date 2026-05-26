package com.geradoretiquetavalidade;

import java.io.IOException;
import java.util.logging.*;

public class LogConfig {

    private static final Logger logger = Logger.getLogger("com.geradoretiquetavalidade");
    private static boolean configurado = false;

    public static void configurarLog() {
        if (configurado) return;

        try {
            // Define o caminho do arquivo de log na Área de Trabalho
            String caminhoLog = System.getProperty("user.home") + "/Desktop/erros.log";
            
            // Cria o manipulador de arquivo (append = true para acumular os erros)
            FileHandler fileHandler = new FileHandler(caminhoLog, true);
            
            // Cria um formatador personalizado para o log não ficar poluído com XML
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    java.time.LocalDateTime agora = java.time.LocalDateTime.now();
                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    
                    return String.format("[%s] [%s] %s %s\n",
                            agora.format(fmt),
                            lr.getLevel(),
                            lr.getMessage(),
                            lr.getThrown() != null ? "\n--> Exceção: " + obterStackTrace(lr.getThrown()) : ""
                    );
                }
            });

            // Adiciona o arquivo ao Logger e desativa os prints automáticos do console se preferir
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            
            configurado = true;
        } catch (IOException | SecurityException e) {
            System.err.println("Não foi possível iniciar o arquivo de log: " + e.getMessage());
        }
    }

    public static Logger getLogger() {
        if (!configurado) configurarLog();
        return logger;
    }

    // Método auxiliar para transformar o erro completo (StackTrace) em texto para o arquivo
    private static String obterStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}