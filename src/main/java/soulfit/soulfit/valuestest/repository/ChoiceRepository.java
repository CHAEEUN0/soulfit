package soulfit.soulfit.valuestest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.valuestest.domain.Choice;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    List<Choice> findByQuestionId(Long questionId);
}
