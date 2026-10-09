package br.com.fiap.petfiap.service;

import br.com.fiap.petfiap.exception.AtendimentoNaoEncontradoException;
import br.com.fiap.petfiap.exception.HorarioOcupadoException;
import br.com.fiap.petfiap.model.Atendimento;
import br.com.fiap.petfiap.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Regras de agenda do PetFiap: agendar, concluir e cancelar atendimentos.
@Service
public class AgendaService {

    private final AtendimentoRepository repository;

    public AgendaService(AtendimentoRepository repository) {
        this.repository = repository;
    }

    // Agenda um novo atendimento: recusa horário já ocupado pelo mesmo pet.
    public Atendimento agendar(Atendimento novo) {
        if (novo.getDataHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar em data/hora no passado");
        }
        List<Atendimento> doPet = repository.findByPetNome(novo.getPetNome());
        if (existeConflitoHorario(novo, doPet)) {
            throw new HorarioOcupadoException(
                    "Pet " + novo.getPetNome() + " já possui atendimento agendado nesse horário");
        }
        return repository.save(novo);
    }

    // Busca pelo id; nunca retorna null, o orElseThrow garante a exceção.
    public Atendimento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AtendimentoNaoEncontradoException("Atendimento não encontrado: " + id));
    }

    // Conclui o atendimento (status AGENDADO -> CONCLUÍDO).
    public Atendimento concluir(Long id) {
        Atendimento atendimento = buscarPorId(id);
        atendimento.concluir();
        return repository.save(atendimento);
    }

    // Cancela o atendimento (status AGENDADO -> CANCELADO).
    public Atendimento cancelar(Long id) {
        Atendimento atendimento = buscarPorId(id);
        atendimento.cancelar();
        return repository.save(atendimento);
    }

    // Lista os atendimentos de um pet.
    public List<Atendimento> buscarPorPet(String petNome) {
        return repository.findByPetNome(petNome);
    }

    // Verifica se existe conflito de horário para um pet.
    private boolean existeConflitoHorario(Atendimento novo, List<Atendimento> atendimentosDoPet) {
        return atendimentosDoPet.stream()
                .anyMatch(a -> a.getDataHora().equals(novo.getDataHora())
                        && Atendimento.AGENDADO.equals(a.getStatus()));
    }
}
