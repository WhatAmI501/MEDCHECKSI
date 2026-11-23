package br.com.cesmac.medcheck.api;

import br.com.cesmac.medcheck.model.*;
import br.com.cesmac.medcheck.model.valueobjects.ItemReceita;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;
import br.com.cesmac.medcheck.repository.MedCheckDataStore;
import br.com.cesmac.medcheck.service.AuthService;
import br.com.cesmac.medcheck.service.ClinicaService;
import br.com.cesmac.medcheck.service.ReceitaService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP REST Nativo em Java (Porta 8080) expondo os endpoints para o BFF Node.js.
 */
public class MedCheckHttpServer {

    private final int porta;
    private final AuthService authService;
    private final ClinicaService clinicaService;
    private final ReceitaService receitaService;
    private HttpServer server;

    public MedCheckHttpServer(int porta) {
        this.porta = porta;
        this.authService = new AuthService();
        this.clinicaService = new ClinicaService();
        this.receitaService = new ReceitaService();
    }

    public void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress(porta), 0);

        // Rota de Health Check
        server.createContext("/api/status", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            enviarJson(exchange, 200, "{\"status\":\"ONLINE\",\"sistema\":\"MEDCHECK Core Java POO\",\"versao\":\"1.0.0\"}");
        });

        // Rota de Autenticação / Login
        server.createContext("/api/auth/login", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = lerCorpo(exchange);
                    Map<String, String> params = extrairJsonSimples(body);
                    String login = params.get("login");
                    String senha = params.get("senha");

                    Usuario u = authService.autenticar(login, senha);
                    if (u != null) {
                        StringBuilder sb = new StringBuilder("{");
                        sb.append("\"sucesso\":true,");
                        sb.append("\"id\":").append(u.getId()).append(",");
                        sb.append("\"nome\":\"").append(u.getNomeCompleto()).append("\",");
                        sb.append("\"perfil\":\"").append(u.getPerfil().name()).append("\",");
                        sb.append("\"descricaoPerfil\":\"").append(u.obterDescricaoPerfil()).append("\"");
                        sb.append("}");
                        enviarJson(exchange, 200, sb.toString());
                    } else {
                        enviarJson(exchange, 401, "{\"sucesso\":false,\"erro\":\"Credenciais inválidas.\"}");
                    }
                } catch (Exception e) {
                    enviarJson(exchange, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Rota de Pacientes (GET / POST)
        server.createContext("/api/pacientes", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                if (path.matches(".*/api/pacientes/\\d+/historico")) {
                    String[] parts = path.split("/");
                    int pacId = Integer.parseInt(parts[3]);
                    Map<String, Object> hist = clinicaService.obterHistoricoLongitudinal(pacId);
                    enviarJson(exchange, 200, serializarHistorico(hist));
                    return;
                }

                // Listar todos os pacientes
                Collection<Paciente> lista = MedCheckDataStore.getInstance().listarTodosPacientes();
                StringBuilder sb = new StringBuilder("[");
                int count = 0;
                for (Paciente p : lista) {
                    if (count++ > 0) sb.append(",");
                    HistoricoClinico h = MedCheckDataStore.getInstance().buscarHistoricoPorPaciente(p.getId());
                    sb.append("{");
                    sb.append("\"id\":").append(p.getId()).append(",");
                    sb.append("\"nome\":\"").append(p.getNomeCompleto()).append("\",");
                    sb.append("\"cpf\":\"").append(p.getCpf().getValorFormatado()).append("\",");
                    sb.append("\"email\":\"").append(p.getEmail()).append("\",");
                    sb.append("\"telefone\":\"").append(p.getTelefone() != null ? p.getTelefone() : "").append("\",");
                    sb.append("\"idade\":").append(p.calcularIdade()).append(",");
                    sb.append("\"sexo\":\"").append(p.getSexo()).append("\",");
                    sb.append("\"peso\":").append(p.getPeso() != null ? p.getPeso() : 0).append(",");
                    sb.append("\"altura\":").append(p.getAltura() != null ? p.getAltura() : 0).append(",");
                    sb.append("\"alergias\":\"").append(h != null ? String.join(", ", h.getAlergiasConhecidas()) : "").append("\"");
                    sb.append("}");
                }
                sb.append("]");
                enviarJson(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = lerCorpo(exchange);
                    Map<String, String> p = extrairJsonSimples(body);
                    String prioridade = p.getOrDefault("prioridadeManchester", p.getOrDefault("prioridade", ""));
                    String queixa = p.getOrDefault("queixa", "");
                    Paciente pac = clinicaService.cadastrarPaciente(
                            p.get("nome"),
                            p.get("cpf"),
                            p.get("email"),
                            p.getOrDefault("senha", "paciente123"),
                            LocalDate.parse(p.get("dataNascimento")),
                            p.get("sexo"),
                            p.get("telefone"),
                            Double.parseDouble(p.getOrDefault("peso", "70")),
                            Double.parseDouble(p.getOrDefault("altura", "1.70")),
                            p.getOrDefault("alergias", ""),
                            p.getOrDefault("doencasCronicas", ""),
                            Boolean.parseBoolean(p.getOrDefault("cardiopatia", "false")),
                            Boolean.parseBoolean(p.getOrDefault("diabetes", "false")),
                            p.getOrDefault("historicoFamiliar", ""),
                            prioridade,
                            queixa
                    );
                    enviarJson(exchange, 201, "{\"sucesso\":true,\"id\":" + pac.getId() + ",\"mensagem\":\"Paciente cadastrado com sucesso!\"}");
                } catch (Exception e) {
                    enviarJson(exchange, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
                }
            }
        });

        // Rota de Triagem e Motor de Manchester
        server.createContext("/api/triagens", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = lerCorpo(exchange);
                    Map<String, String> p = extrairJsonSimples(body);
                    int pacienteId = Integer.parseInt(p.get("pacienteId"));
                    String queixa = p.get("queixa");
                    String local = p.get("local");
                    String intensidade = p.get("intensidade");
                    String duracao = p.get("duracao");
                    String frequencia = p.get("frequencia");

                    List<String> sintomas = new ArrayList<>();
                    if (p.containsKey("sintomasAssociados")) {
                        sintomas.addAll(Arrays.asList(p.get("sintomasAssociados").split(",")));
                    }

                    Double temp = p.containsKey("temperatura") && !p.get("temperatura").isEmpty() ? Double.parseDouble(p.get("temperatura")) : null;
                    Integer paSis = p.containsKey("paSistolica") && !p.get("paSistolica").isEmpty() ? Integer.parseInt(p.get("paSistolica")) : null;
                    Integer paDia = p.containsKey("paDiastolica") && !p.get("paDiastolica").isEmpty() ? Integer.parseInt(p.get("paDiastolica")) : null;
                    Integer fc = p.containsKey("fc") && !p.get("fc").isEmpty() ? Integer.parseInt(p.get("fc")) : null;
                    Integer spo2 = p.containsKey("spo2") && !p.get("spo2").isEmpty() ? Integer.parseInt(p.get("spo2")) : null;
                    Integer glic = p.containsKey("glicemia") && !p.get("glicemia").isEmpty() ? Integer.parseInt(p.get("glicemia")) : null;

                    SinaisVitais vitais = new SinaisVitais(temp, paSis, paDia, fc, spo2, glic);

                    Triagem triagem = clinicaService.realizarTriagemEClassificacao(
                            pacienteId, queixa, local, intensidade, duracao, frequencia, sintomas, vitais
                    );

                    ClassificacaoRisco c = triagem.getClassificacaoRisco();
                    StringBuilder sb = new StringBuilder("{");
                    sb.append("\"sucesso\":true,");
                    sb.append("\"triagemId\":").append(triagem.getId()).append(",");
                    sb.append("\"status\":\"").append(triagem.getStatus().name()).append("\",");
                    sb.append("\"corManchester\":\"").append(c.getCorManchester().name()).append("\",");
                    sb.append("\"prioridadeRotulo\":\"").append(c.getCorManchester().getRotulo()).append("\",");
                    sb.append("\"tempoAlvoMinutos\":").append(c.getTempoAlvoMinutos()).append(",");
                    sb.append("\"orientacaoPaciente\":\"").append(escaparJson(c.getOrientacaoPaciente())).append("\",");
                    sb.append("\"encaminhamentoRecomendado\":\"").append(escaparJson(c.getEncaminhamentoRecomendado())).append("\",");
                    sb.append("\"sinaisAlerta\":\"").append(escaparJson(c.getSinaisAlerta())).append("\",");
                    sb.append("\"discriminadores\":[\"").append(String.join("\",\"", c.getDiscriminadoresIdentificados())).append("\"]");
                    sb.append("}");
                    enviarJson(exchange, 200, sb.toString());
                } catch (Exception e) {
                    enviarJson(exchange, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
                }
            }
        });

        // Rota da Fila Inteligente de Teleatendimento [RF021]
        server.createContext("/api/fila", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            List<Map<String, Object>> fila = clinicaService.obterFilaPriorizadaTeleatendimento();
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (Map<String, Object> item : fila) {
                if (count++ > 0) sb.append(",");
                sb.append("{");
                sb.append("\"atendimentoId\":").append(item.get("atendimentoId")).append(",");
                sb.append("\"triagemId\":").append(item.get("triagemId")).append(",");
                sb.append("\"pacienteId\":").append(item.get("pacienteId")).append(",");
                sb.append("\"pacienteNome\":\"").append(item.get("pacienteNome")).append("\",");
                sb.append("\"pacienteIdade\":").append(item.get("pacienteIdade")).append(",");
                sb.append("\"queixa\":\"").append(escaparJson(item.get("queixa").toString())).append("\",");
                sb.append("\"corManchester\":\"").append(item.get("corManchester")).append("\",");
                sb.append("\"prioridadeRotulo\":\"").append(item.get("prioridadeRotulo")).append("\",");
                sb.append("\"tempoAlvoMinutos\":").append(item.get("tempoAlvoMinutos")).append(",");
                sb.append("\"status\":\"").append(item.get("status")).append("\"");
                sb.append("}");
            }
            sb.append("]");
            enviarJson(exchange, 200, sb.toString());
        });

        // Rota de Emissão e Validação de Receitas [RF008 / RF024 / RF030]
        server.createContext("/api/receitas", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && path.contains("/validar")) {
                String query = exchange.getRequestURI().getQuery();
                String hash = null;
                if (query != null && query.contains("hash=")) {
                    hash = query.split("hash=")[1].split("&")[0];
                }
                ReceitaMedica rec = receitaService.validarReceitaPublica(hash);
                if (rec != null) {
                    Paciente p = MedCheckDataStore.getInstance().buscarPacientePorId(rec.getPacienteId());
                    Medico m = (Medico) MedCheckDataStore.getInstance().buscarUsuarioPorId(rec.getMedicoId());
                    StringBuilder sb = new StringBuilder("{");
                    sb.append("\"valida\":true,");
                    sb.append("\"id\":").append(rec.getId()).append(",");
                    sb.append("\"hash\":\"").append(rec.getHashValidacao()).append("\",");
                    sb.append("\"pacienteNome\":\"").append(p != null ? p.getNomeCompleto() : "").append("\",");
                    sb.append("\"medicoNome\":\"").append(m != null ? m.getNomeCompleto() : "").append("\",");
                    sb.append("\"crm\":\"").append(m != null ? m.getRegistroConselho() : "").append("\",");
                    sb.append("\"dataEmissao\":\"").append(rec.getDataEmissao()).append("\",");
                    sb.append("\"status\":\"").append(rec.getStatus().name()).append("\"");
                    sb.append("}");
                    enviarJson(exchange, 200, sb.toString());
                } else {
                    enviarJson(exchange, 404, "{\"valida\":false,\"erro\":\"Receita não localizada ou hash inválido.\"}");
                }
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && !path.contains("/validar")) {
                Collection<ReceitaMedica> lista = MedCheckDataStore.getInstance().listarTodasReceitas();
                StringBuilder sb = new StringBuilder("[");
                int c = 0;
                for (ReceitaMedica r : lista) {
                    if (c++ > 0) sb.append(",");
                    Paciente p = MedCheckDataStore.getInstance().buscarPacientePorId(r.getPacienteId());
                    Medico m = (Medico) MedCheckDataStore.getInstance().buscarUsuarioPorId(r.getMedicoId());
                    sb.append("{");
                    sb.append("\"id\":").append(r.getId()).append(",");
                    sb.append("\"atendimentoId\":").append(r.getAtendimentoId()).append(",");
                    sb.append("\"pacienteId\":").append(r.getPacienteId()).append(",");
                    sb.append("\"pacienteNome\":\"").append(p != null ? escaparJson(p.getNomeCompleto()) : "Paciente").append("\",");
                    sb.append("\"medicoNome\":\"").append(m != null ? escaparJson(m.getNomeCompleto()) : "Dr. Roberto Carlos da Silva").append("\",");
                    sb.append("\"crm\":\"").append(m != null ? escaparJson(m.getRegistroConselho()) : "CRM/AL 14892").append("\",");
                    sb.append("\"dataEmissao\":\"").append(r.getDataEmissao()).append("\",");
                    sb.append("\"status\":\"").append(r.getStatus().name()).append("\",");
                    sb.append("\"observacoes\":\"").append(escaparJson(r.getObservacoes())).append("\",");
                    sb.append("\"hash\":\"").append(r.getHashValidacao()).append("\",");
                    sb.append("\"hashValidacao\":\"").append(r.getHashValidacao()).append("\",");
                    sb.append("\"itens\":[");
                    int itemIdx = 0;
                    for (ItemReceita item : r.getItens()) {
                        if (itemIdx++ > 0) sb.append(",");
                        sb.append("{");
                        sb.append("\"medicamento\":\"").append(escaparJson(item.getMedicamento())).append("\",");
                        sb.append("\"principioAtivo\":\"").append(escaparJson(item.getPrincipioAtivo())).append("\",");
                        sb.append("\"dosagem\":\"").append(escaparJson(item.getDosagem())).append("\",");
                        sb.append("\"posologia\":\"").append(escaparJson(item.getPosologia())).append("\",");
                        sb.append("\"quantidade\":\"").append(escaparJson(item.getQuantidade())).append("\",");
                        sb.append("\"via\":\"").append(escaparJson(item.getViaAdministracao())).append("\"");
                        sb.append("}");
                    }
                    sb.append("]");
                    sb.append("}");
                }
                sb.append("]");
                enviarJson(exchange, 200, sb.toString());
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = lerCorpo(exchange);
                    Map<String, String> p = extrairJsonSimples(body);
                    int atendimentoId = Integer.parseInt(p.getOrDefault("atendimentoId", "0"));
                    int pacienteId = Integer.parseInt(p.getOrDefault("pacienteId", "0"));
                    int medicoId = Integer.parseInt(p.getOrDefault("medicoId", "2"));
                    String observacoes = p.getOrDefault("observacoes", "");
                    boolean confirmacaoAlergia = Boolean.parseBoolean(p.getOrDefault("confirmacaoAlergia", "false"));

                    List<ItemReceita> itens = new ArrayList<>();
                    // Suporta medicamento único ou múltiplos
                    String medNome = p.get("medicamento");
                    String principio = p.get("principioAtivo");
                    String dosagem = p.getOrDefault("dosagem", "1 comp");
                    String posologia = p.getOrDefault("posologia", "1x ao dia");
                    String qtd = p.getOrDefault("quantidade", "1 caixa");
                    String via = p.getOrDefault("via", "Oral");

                    if (medNome != null && principio != null) {
                        itens.add(new ItemReceita(medNome, principio, dosagem, posologia, qtd, via));
                    }

                    ReceitaMedica receita = receitaService.emitirReceita(atendimentoId, pacienteId, medicoId, itens, observacoes, confirmacaoAlergia);

                    StringBuilder sb = new StringBuilder("{");
                    sb.append("\"sucesso\":true,");
                    sb.append("\"receitaId\":").append(receita.getId()).append(",");
                    sb.append("\"hash\":\"").append(receita.getHashValidacao()).append("\",");
                    sb.append("\"urlQrCode\":\"").append(receita.getUrlQrCode()).append("\",");
                    sb.append("\"mensagem\":\"Receita emitida e assinada com sucesso!\"");
                    sb.append("}");
                    enviarJson(exchange, 201, sb.toString());
                } catch (IllegalStateException e) {
                    // Alerta de conflito de alergia [RS007]
                    enviarJson(exchange, 409, "{\"sucesso\":false,\"alertaAlergia\":true,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
                } catch (Exception e) {
                    enviarJson(exchange, 400, "{\"sucesso\":false,\"erro\":\"" + escaparJson(e.getMessage()) + "\"}");
                }
            }
        });

        // Rota de Dashboard e Indicadores Clínicos [RF012]
        server.createContext("/api/dashboard", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            Map<String, Object> metricas = clinicaService.obterMetricasDashboard();
            @SuppressWarnings("unchecked")
            Map<String, Long> dist = (Map<String, Long>) metricas.get("distribuicaoManchester");

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"totalTriagens\":").append(metricas.get("totalTriagens")).append(",");
            sb.append("\"totalPacientes\":").append(metricas.get("totalPacientes")).append(",");
            sb.append("\"totalReceitas\":").append(metricas.get("totalReceitas")).append(",");
            sb.append("\"totalAtendimentos\":").append(metricas.get("totalAtendimentos")).append(",");
            sb.append("\"distribuicao\":{");
            int c = 0;
            for (Map.Entry<String, Long> e : dist.entrySet()) {
                if (c++ > 0) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            }
            sb.append("}}");
            enviarJson(exchange, 200, sb.toString());
        });

        // Rota de Logs de Auditoria [RF032 / RS006]
        server.createContext("/api/auditoria", exchange -> {
            adicionarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            List<LogAuditoria> logs = MedCheckDataStore.getInstance().listarLogsAuditoria();
            StringBuilder sb = new StringBuilder("[");
            int c = 0;
            for (LogAuditoria log : logs) {
                if (c++ > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(log.getId()).append(",");
                sb.append("\"modulo\":\"").append(log.getModuloAcao()).append("\",");
                sb.append("\"operacao\":\"").append(log.getTipoOperacao()).append("\",");
                sb.append("\"detalhes\":\"").append(escaparJson(log.getDetalhes())).append("\",");
                sb.append("\"ip\":\"").append(log.getIpOrigem()).append("\",");
                sb.append("\"dataHora\":\"").append(log.getDataHora()).append("\"");
                sb.append("}");
            }
            sb.append("]");
            enviarJson(exchange, 200, sb.toString());
        });

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println(">>> [JAVA] MedCheck REST API Server iniciado com sucesso na porta " + porta);
    }

    public void parar() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void adicionarCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void enviarJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String lerCorpo(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static Map<String, String> extrairJsonSimples(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String limpo = json.trim();
        if (limpo.startsWith("{") && limpo.endsWith("}")) {
            limpo = limpo.substring(1, limpo.length() - 1);
        }
        String[] pairs = limpo.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim();
                if (v.startsWith("\"") && v.endsWith("\"")) {
                    v = v.substring(1, v.length() - 1);
                }
                map.put(k, v);
            }
        }
        return map;
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @SuppressWarnings("unchecked")
    private static String serializarHistorico(Map<String, Object> map) {
        Paciente p = (Paciente) map.get("paciente");
        HistoricoClinico h = (HistoricoClinico) map.get("historicoClinico");
        List<Triagem> triagens = (List<Triagem>) map.get("triagens");
        List<ReceitaMedica> receitas = (List<ReceitaMedica>) map.get("receitas");

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"paciente\":{");
        if (p != null) {
            sb.append("\"id\":").append(p.getId()).append(",");
            sb.append("\"nome\":\"").append(p.getNomeCompleto()).append("\",");
            sb.append("\"cpf\":\"").append(p.getCpf().getValorFormatado()).append("\",");
            sb.append("\"idade\":").append(p.calcularIdade()).append(",");
            sb.append("\"alergias\":\"").append(h != null ? String.join(", ", h.getAlergiasConhecidas()) : "").append("\",");
            sb.append("\"doencasCronicas\":\"").append(h != null && h.getDescricaoDoencasCronicas() != null ? escaparJson(h.getDescricaoDoencasCronicas()) : "").append("\"");
        }
        sb.append("},\"triagens\":[");
        int countT = 0;
        for (Triagem t : triagens) {
            if (countT++ > 0) sb.append(",");
            ClassificacaoRisco c = t.getClassificacaoRisco();
            sb.append("{");
            sb.append("\"id\":").append(t.getId()).append(",");
            sb.append("\"queixa\":\"").append(escaparJson(t.getQueixaPrincipal())).append("\",");
            sb.append("\"intensidade\":\"").append(t.getIntensidade() != null ? t.getIntensidade().name() : "").append("\",");
            sb.append("\"corManchester\":\"").append(c != null ? c.getCorManchester().name() : "AZUL").append("\",");
            sb.append("\"prioridadeRotulo\":\"").append(c != null ? c.getCorManchester().getRotulo() : "").append("\",");
            sb.append("\"data\":\"").append(t.getDataHoraInicio()).append("\",");
            sb.append("\"resumo\":\"").append(escaparJson(t.getResumoEstruturado())).append("\"");
            sb.append("}");
        }
        sb.append("],\"receitas\":[");
        int countR = 0;
        for (ReceitaMedica r : receitas) {
            if (countR++ > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":").append(r.getId()).append(",");
            sb.append("\"hash\":\"").append(r.getHashValidacao()).append("\",");
            sb.append("\"status\":\"").append(r.getStatus().name()).append("\",");
            sb.append("\"data\":\"").append(r.getDataEmissao()).append("\",");
            sb.append("\"itens\":[");
            int cItem = 0;
            for (ItemReceita item : r.getItens()) {
                if (cItem++ > 0) sb.append(",");
                sb.append("{");
                sb.append("\"medicamento\":\"").append(item.getMedicamento()).append("\",");
                sb.append("\"principioAtivo\":\"").append(item.getPrincipioAtivo()).append("\",");
                sb.append("\"posologia\":\"").append(item.getPosologia()).append("\"");
                sb.append("}");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }
}
