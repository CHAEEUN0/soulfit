package soulfit.soulfit.valuestest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.valuestest.domain.TestAnswer;

import java.util.List;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {

    List<TestAnswer> findBySessionId(Long sessionId);

    List<TestAnswer> findByQuestionId(Long questionId);

}
