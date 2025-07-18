package soulfit.soulfit.valuestest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.valuestest.domain.TestQuestion;
import soulfit.soulfit.valuestest.domain.TestType;

import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTestType(TestType testType);
}
