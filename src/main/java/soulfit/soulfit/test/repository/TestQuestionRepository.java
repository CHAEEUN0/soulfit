package soulfit.soulfit.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.test.domain.TestQuestion;
import soulfit.soulfit.test.domain.TestType;

import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTestType(TestType testType);
}
