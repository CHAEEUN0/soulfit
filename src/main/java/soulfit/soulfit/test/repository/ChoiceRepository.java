package soulfit.soulfit.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.test.domain.Choice;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    List<Choice> findByQuestionId(Long questionId);
}
