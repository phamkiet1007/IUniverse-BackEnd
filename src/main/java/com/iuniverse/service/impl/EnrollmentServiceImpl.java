@Override
@Transactional
public void enrollByCode(String joinCode, Long studentId) {

    // 1. Validate join code
    if (joinCode == null || joinCode.trim().isEmpty()) {
        throw new InvalidDataException("Invalid join code!");
    }

    joinCode = joinCode.trim();

    // 2. Find course by join code
    Course course = courseRepository.findByJoinCode(joinCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Course not found with code: " + joinCode));

    // 3. Check if student already enrolled
    if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, course.getId())) {
        throw new InvalidDataException("You have already enrolled in this course!");
    }

    // 4. Find student
    User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found!"));

    // 5. Create enrollment
    Enrollment enrollment = new Enrollment();
    enrollment.setStudent(student);
    enrollment.setCourse(course);

    enrollmentRepository.save(enrollment);
}