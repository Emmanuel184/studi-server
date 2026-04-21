package com.studi.server.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import com.studi.server.model.AppUser;
import com.studi.server.model.Step;
import com.studi.server.model.StudyPlan;
import com.studi.server.repository.StepRepository;
import com.studi.server.repository.StudyPlanRepository;
import com.studi.server.repository.UserRepository;

public final class InMemoryRepositories {

    private InMemoryRepositories() {}

    public static Fakes fakes() {
        return new Fakes();
    }

    public static final class Fakes {

        private final Map<Long, AppUser> users = new LinkedHashMap<>();
        private final Map<Long, Step> steps = new LinkedHashMap<>();
        private final Map<Long, StudyPlan> studyPlans = new LinkedHashMap<>();
        private long nextUserId = 1;
        private long nextStepId = 1;
        private long nextStudyPlanId = 1;

        private final UserRepository userRepository = proxy(UserRepository.class, this::handleUserRepository);
        private final StepRepository stepRepository = proxy(StepRepository.class, this::handleStepRepository);
        private final StudyPlanRepository studyPlanRepository = proxy(
                StudyPlanRepository.class,
                this::handleStudyPlanRepository);

        public UserRepository userRepository() {
            return userRepository;
        }

        public StepRepository stepRepository() {
            return stepRepository;
        }

        public StudyPlanRepository studyPlanRepository() {
            return studyPlanRepository;
        }

        public Map<Long, Step> steps() {
            return steps;
        }

        public Map<Long, StudyPlan> studyPlans() {
            return studyPlans;
        }

        public AppUser saveUser(String username) {
            AppUser user = new AppUser(username, "password-hash");
            ReflectionTestUtils.setField(user, "id", nextUserId++);
            users.put(user.getId(), user);
            return user;
        }

        public StudyPlan saveStudyPlan(AppUser user, String title, String goal) {
            StudyPlan studyPlan = new StudyPlan();
            studyPlan.setUser(user);
            studyPlan.setTitle(title);
            studyPlan.setGoal(goal);
            return saveStudyPlan(studyPlan);
        }

        private Object handleUserRepository(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findById" -> Optional.ofNullable(users.get((Long) args[0]));
                case "findByUsername" -> users.values()
                        .stream()
                        .filter(user -> user.getUsername().equals(args[0]))
                        .findFirst();
                case "existsByUsername" -> users.values()
                        .stream()
                        .anyMatch(user -> user.getUsername().equals(args[0]));
                case "save" -> saveUser((AppUser) args[0]);
                default -> defaultReturn(method);
            };
        }

        private Object handleStepRepository(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "save" -> saveStep((Step) args[0]);
                case "findById" -> Optional.ofNullable(steps.get((Long) args[0]));
                case "findByIdAndUserId" -> findStepByIdAndUserId((Long) args[0], (Long) args[1]);
                case "findAllByUserIdOrderByPositionAsc" -> steps.values()
                        .stream()
                        .filter(step -> step.getUser().getId().equals(args[0]))
                        .sorted(Comparator.comparingInt(Step::getPosition))
                        .toList();
                case "findAllByStudyPlanIdAndUserIdOrderByPositionAsc" -> steps.values()
                        .stream()
                        .filter(step -> step.getStudyPlan() != null)
                        .filter(step -> step.getStudyPlan().getId().equals(args[0]))
                        .filter(step -> step.getUser().getId().equals(args[1]))
                        .sorted(Comparator.comparingInt(Step::getPosition))
                        .toList();
                case "delete" -> {
                    steps.remove(((Step) args[0]).getId());
                    yield null;
                }
                default -> defaultReturn(method);
            };
        }

        private Object handleStudyPlanRepository(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "save" -> saveStudyPlan((StudyPlan) args[0]);
                case "findById" -> Optional.ofNullable(studyPlans.get((Long) args[0]));
                case "findByIdAndUserId" -> findStudyPlanByIdAndUserId((Long) args[0], (Long) args[1]);
                default -> defaultReturn(method);
            };
        }

        private AppUser saveUser(AppUser user) {
            if (user.getId() == null) {
                ReflectionTestUtils.setField(user, "id", nextUserId++);
            }
            users.put(user.getId(), user);
            return user;
        }

        private Step saveStep(Step step) {
            if (step.getId() == null) {
                ReflectionTestUtils.setField(step, "id", nextStepId++);
            }
            steps.put(step.getId(), step);
            return step;
        }

        private StudyPlan saveStudyPlan(StudyPlan studyPlan) {
            if (studyPlan.getId() == null) {
                ReflectionTestUtils.setField(studyPlan, "id", nextStudyPlanId++);
            }
            studyPlans.put(studyPlan.getId(), studyPlan);
            return studyPlan;
        }

        private Optional<Step> findStepByIdAndUserId(Long stepId, Long userId) {
            Step step = steps.get(stepId);
            if (step == null || !step.getUser().getId().equals(userId)) {
                return Optional.empty();
            }
            return Optional.of(step);
        }

        private Optional<StudyPlan> findStudyPlanByIdAndUserId(Long planId, Long userId) {
            StudyPlan studyPlan = studyPlans.get(planId);
            if (studyPlan == null || !studyPlan.getUser().getId().equals(userId)) {
                return Optional.empty();
            }
            return Optional.of(studyPlan);
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
    }

    private static Object defaultReturn(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(boolean.class)) {
            return false;
        }
        if (returnType.equals(void.class)) {
            return null;
        }
        if (List.class.isAssignableFrom(returnType)) {
            return new ArrayList<>();
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.empty();
        }
        return null;
    }
}
