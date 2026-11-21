package br.com.cesmac.medcheck.repository;

import br.com.cesmac.medcheck.model.*;
import br.com.cesmac.medcheck.model.enums.*;
import br.com.cesmac.medcheck.model.valueobjects.CPF;
import br.com.cesmac.medcheck.model.valueobjects.ItemReceita;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repositório Central de Dados do Sistema MEDCHECK.
 * Mantém os dados normalizados em memória com persistência e integridade referencial.
 */
public class MedCheckDataStore {
    private static final MedCheckDataStore INSTANCE = new MedCheckDataStore();

    public static MedCheckDataStore getInstance() {
        return INSTANCE;
    }

    private final AtomicInteger idUsuarioSeq = new AtomicInteger(10);
    private final AtomicInteger idTriagemSeq = new AtomicInteger(10);
    private final AtomicInteger idClassificacaoSeq = new AtomicInteger(10);
    private final AtomicInteger idAtendimentoSeq = new AtomicInteger(10);
    private final AtomicInteger idReceitaSeq = new AtomicInteger(10);
    private final AtomicInteger idLogSeq = new AtomicInteger(10);

    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<String, Usuario> usuariosPorCpf = new ConcurrentHashMap<>();
    private final Map<String, Usuario> usuariosPorEmail = new ConcurrentHashMap<>();
    private final Map<Integer, Paciente> pacientes = new ConcurrentHashMap<>();
    private final Map<Integer, ProfissionalSaude> profissionais = new ConcurrentHashMap<>();
    private final Map<Integer, HistoricoClinico> historicosPorPaciente = new ConcurrentHashMap<>();
    private final Map<Integer, Triagem> triagens = new ConcurrentHashMap<>();
    private final Map<Integer, ClassificacaoRisco> classificacoesPorTriagem = new ConcurrentHashMap<>();
    private final Map<Integer, AtendimentoMedico> atendimentos = new ConcurrentHashMap<>();
    private final Map<Integer, ReceitaMedica> receitas = new ConcurrentHashMap<>();
    private final Map<String, ReceitaMedica> receitasPorHash = new ConcurrentHashMap<>();
    private final List<LogAuditoria> logsAuditoria = Collections.synchronizedList(new ArrayList<>());

    private MedCheckDataStore() {
        carregarDadosIniciaisSeed();
    }

    private void carregarDadosIniciaisSeed() {
        try {
            // 1. Administrador
            CPF cpfAdmin = new CPF("000.000.000-00");
            Usuario admin = new Usuario(1, "Administrador MedCheck", cpfAdmin, "admin@medcheck.com.br", "admin123", PerfilAcesso.ADMINISTRADOR) {
                @Override
                public String obterDescricaoPerfil() {
                    return "Administrador do Sistema";
                }
            };
            salvarUsuario(admin);

            // 2. Médico
            CPF cpfMed = new CPF("111.222.333-44");
            Medico med = new Medico(2, "Dr. Roberto Carlos da Silva", cpfMed, "roberto.med@medcheck.com.br", "medico123", "CRM/AL 14892", "Clínica Geral e Emergência");
            salvarUsuario(med);
            profissionais.put(2, med);

            // 3. Enfermeira
            CPF cpfEnf = new CPF("555.666.777-88");
            Enfermeiro enf = new Enfermeiro(3, "Enfª Juliana Paes Mendes", cpfEnf, "juliana.enf@medcheck.com.br", "enfermeira123", "COREN/AL 98765", "Enfermagem e Triagem");
            salvarUsuario(enf);
            profissionais.put(3, enf);

            // 4. Pacientes
            CPF cpfPac1 = new CPF("123.456.789-01");
            Paciente pac1 = new Paciente(4, "Mariana Oliveira Costa", cpfPac1, "mariana@email.com", "paciente123",
                    LocalDate.of(1995, 4, 12), "Feminino", "(82) 98888-1111", 62.5, 1.65);
            HistoricoClinico hist1 = new HistoricoClinico(1, 4);
            hist1.setPossuiDoencaCronica(false);
            hist1.setDescricaoDoencasCronicas("Nenhuma doença crônica");
            hist1.setHistoricoFamiliar("Hipertensão materna");
            hist1.setAlergiasPorString("Dipirona, Anti-inflamatórios");
            hist1.setMedicamentosEmUso("Nenhum de uso contínuo");
            pac1.setHistoricoClinico(hist1);
            salvarPaciente(pac1, hist1);

            CPF cpfPac2 = new CPF("234.567.890-12");
            Paciente pac2 = new Paciente(5, "José Ferreira dos Santos", cpfPac2, "jose@email.com", "paciente123",
                    LocalDate.of(1968, 11, 20), "Masculino", "(82) 99999-2222", 84.0, 1.74);
            HistoricoClinico hist2 = new HistoricoClinico(2, 5);
            hist2.setPossuiDoencaCronica(true);
            hist2.setDescricaoDoencasCronicas("Hipertensão Arterial e Diabetes Tipo 2");
            hist2.setPossuiCardiopatia(true);
            hist2.setPossuiDiabetes(true);
            hist2.setHistoricoFamiliar("Infarto agudo do miocárdio (pai)");
            hist2.setAlergiasPorString("Penicilina, Amoxicilina");
            hist2.setMedicamentosEmUso("Losartana 50mg, Metformina 850mg");
            pac2.setHistoricoClinico(hist2);
            salvarPaciente(pac2, hist2);

            CPF cpfPac3 = new CPF("345.678.901-23");
            Paciente pac3 = new Paciente(6, "Ana Beatriz Lima", cpfPac3, "ana@email.com", "paciente123",
                    LocalDate.of(2001, 8, 30), "Feminino", "(82) 97777-3333", 58.0, 1.60);
            HistoricoClinico hist3 = new HistoricoClinico(3, 6);
            hist3.setPossuiDoencaCronica(false);
            hist3.setAlergiasPorString("");
            hist3.setMedicamentosEmUso("Nenhum");
            pac3.setHistoricoClinico(hist3);
            salvarPaciente(pac3, hist3);

            // 5. Triagens e Classificações de Demonstração
            Triagem t1 = new Triagem(1, 5, "Dor Torácica");
            t1.setLocalSintoma("Precordial com irradiação para braço esquerdo");
            t1.setIntensidade(IntensidadeSintoma.INTENSA);
            t1.setDuracaoInicio("Há 40 minutos");
            t1.setFrequencia("CONTINUA");
            t1.adicionarSintomaAssociado("Sudorese fria");
            t1.adicionarSintomaAssociado("Falta de ar");
            t1.setSinaisVitais(new SinaisVitais(36.8, 160, 100, 115, 93, 140));
            t1.concluirTriagem("Paciente hipertenso e cardiopata com dor torácica típica com características anginosas.");
            triagens.put(1, t1);

            ClassificacaoRisco c1 = new ClassificacaoRisco(1, 1, NivelPrioridade.MUITO_URGENTE, CorManchester.LARANJA,
                    "Procure atendimento imediato ou aguarde teleconsulta prioritária.",
                    "Teleconsulta prioritária em até 10 minutos com regulação para emergência.",
                    "Piora rápida da dor, sudorese intensa ou perda de consciência.");
            c1.adicionarDiscriminador("Dor típica precordial com irradiação; Sudorese e taquicardia.");
            classificacoesPorTriagem.put(1, c1);
            t1.setClassificacaoRisco(c1);

            AtendimentoMedico at1 = new AtendimentoMedico(1, 1, 5);
            at1.assumirAtendimento(2);
            atendimentos.put(1, at1);

            // Triagem 2 (Amarela)
            Triagem t2 = new Triagem(2, 4, "Cefaleia Intensa");
            t2.setLocalSintoma("Frontal e retro-orbitária");
            t2.setIntensidade(IntensidadeSintoma.MODERADA);
            t2.setDuracaoInicio("Há 2 dias");
            t2.setFrequencia("CONTINUA");
            t2.adicionarSintomaAssociado("Fotofobia");
            t2.adicionarSintomaAssociado("Febre");
            t2.setSinaisVitais(new SinaisVitais(38.6, 120, 80, 88, 98, 95));
            t2.concluirTriagem("Cefaleia aguda com febre de 38.6°C, sem rigidez de nuca.");
            triagens.put(2, t2);

            ClassificacaoRisco c2 = new ClassificacaoRisco(2, 2, NivelPrioridade.URGENTE, CorManchester.AMARELO,
                    "Mantenha repouso e hidratação oral. Aguarde atendimento médico.",
                    "Teleconsulta em até 50 minutos.",
                    "Confusão mental, vômitos em jato ou rigidez de nuca.");
            c2.adicionarDiscriminador("Cefaleia moderada com temperatura de 38.6°C.");
            classificacoesPorTriagem.put(2, c2);
            t2.setClassificacaoRisco(c2);

            AtendimentoMedico at2 = new AtendimentoMedico(2, 2, 4);
            atendimentos.put(2, at2);

            // Receita de Exemplo com Hash de Validação (RF008 / RF024)
            ReceitaMedica rec1 = new ReceitaMedica(1, 1, 5, 2, "Paciente orientado quanto a cuidados imediatos.");
            rec1.adicionarItem(new ItemReceita("Isordil Sublingual", "Dinitrato de Isossorbida", "5mg", "1 comp sublingual se dor", "1 caixa", "Sublingual"));
            rec1.adicionarItem(new ItemReceita("AAS", "Ácido Acetilsalicílico", "100mg", "1 comp após almoço", "1 caixa", "Oral"));
            receitas.put(1, rec1);
            receitasPorHash.put(rec1.getHashValidacao(), rec1);

            // Log inicial de auditoria
            registrarLog(new LogAuditoria(1, 1, 5, "TRIAGEM", "INSERCAO", "Classificação de Manchester: LARANJA", "127.0.0.1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void salvarUsuario(Usuario u) {
        if (u.getId() == null) {
            u.setId(idUsuarioSeq.incrementAndGet());
        }
        usuarios.put(u.getId(), u);
        usuariosPorCpf.put(u.getCpf().getValorLimpo(), u);
        usuariosPorEmail.put(u.getEmail(), u);
    }

    public void salvarPaciente(Paciente p, HistoricoClinico h) {
        salvarUsuario(p);
        pacientes.put(p.getId(), p);
        if (h != null) {
            historicosPorPaciente.put(p.getId(), h);
            p.setHistoricoClinico(h);
        }
    }

    public void registrarLog(LogAuditoria log) {
        if (log.getId() == null) {
            log.setId(idLogSeq.incrementAndGet());
        }
        logsAuditoria.add(0, log); // Adiciona no início
    }

    // Acessores de Repositório
    public Usuario buscarUsuarioPorId(Integer id) { return usuarios.get(id); }
    public Usuario buscarUsuarioPorCpf(String cpfLimpo) { return usuariosPorCpf.get(cpfLimpo); }
    public Usuario buscarUsuarioPorEmail(String email) { return usuariosPorEmail.get(email.toLowerCase()); }
    public Paciente buscarPacientePorId(Integer id) { return pacientes.get(id); }
    public Collection<Paciente> listarTodosPacientes() { return pacientes.values(); }
    public HistoricoClinico buscarHistoricoPorPaciente(Integer pacienteId) { return historicosPorPaciente.get(pacienteId); }

    public int gerarProximoIdTriagem() { return idTriagemSeq.incrementAndGet(); }
    public void salvarTriagem(Triagem t) { triagens.put(t.getId(), t); }
    public Triagem buscarTriagemPorId(Integer id) { return triagens.get(id); }
    public Collection<Triagem> listarTodasTriagens() { return triagens.values(); }

    public int gerarProximoIdClassificacao() { return idClassificacaoSeq.incrementAndGet(); }
    public void salvarClassificacao(ClassificacaoRisco c) { classificacoesPorTriagem.put(c.getTriagemId(), c); }
    public ClassificacaoRisco buscarClassificacaoPorTriagem(Integer triagemId) { return classificacoesPorTriagem.get(triagemId); }

    public int gerarProximoIdAtendimento() { return idAtendimentoSeq.incrementAndGet(); }
    public void salvarAtendimento(AtendimentoMedico a) { atendimentos.put(a.getId(), a); }
    public AtendimentoMedico buscarAtendimentoPorId(Integer id) { return atendimentos.get(id); }
    public Collection<AtendimentoMedico> listarTodosAtendimentos() { return atendimentos.values(); }

    public int gerarProximoIdReceita() { return idReceitaSeq.incrementAndGet(); }
    public void salvarReceita(ReceitaMedica r) {
        receitas.put(r.getId(), r);
        receitasPorHash.put(r.getHashValidacao(), r);
    }
    public ReceitaMedica buscarReceitaPorId(Integer id) { return receitas.get(id); }
    public ReceitaMedica buscarReceitaPorHash(String hash) { return receitasPorHash.get(hash); }
    public Collection<ReceitaMedica> listarTodasReceitas() { return receitas.values(); }

    public List<LogAuditoria> listarLogsAuditoria() { return Collections.unmodifiableList(logsAuditoria); }
}
