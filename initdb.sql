-- ==============================================================================
-- 1. DỌN DẸP BẢNG CŨ (Sử dụng CASCADE để tự động xóa các khóa ngoại liên quan)
-- ==============================================================================
DROP TABLE IF EXISTS "tbl_student_answer" CASCADE;
DROP TABLE IF EXISTS "tbl_submission" CASCADE;
DROP TABLE IF EXISTS "tbl_question" CASCADE;
DROP TABLE IF EXISTS "tbl_problem_set" CASCADE;
DROP TABLE IF EXISTS "tbl_module_material" CASCADE;
DROP TABLE IF EXISTS "tbl_material" CASCADE;
DROP TABLE IF EXISTS "tbl_module" CASCADE;
DROP TABLE IF EXISTS "tbl_rating" CASCADE;
DROP TABLE IF EXISTS "tbl_enrollment" CASCADE;
DROP TABLE IF EXISTS "tbl_course" CASCADE;
DROP TABLE IF EXISTS "tbl_semester" CASCADE;
DROP TABLE IF EXISTS "tbl_student" CASCADE;
DROP TABLE IF EXISTS "tbl_teacher" CASCADE;

-- Dọn dẹp cả các bảng RBAC cũ nếu Database vẫn còn lưu
DROP TABLE IF EXISTS "tbl_role_has_permission" CASCADE;
DROP TABLE IF EXISTS "tbl_user_has_role" CASCADE;
DROP TABLE IF EXISTS "tbl_group_has_user" CASCADE;
DROP TABLE IF EXISTS "tbl_group" CASCADE;
DROP TABLE IF EXISTS "tbl_permission" CASCADE;
DROP TABLE IF EXISTS "tbl_role" CASCADE;

DROP TABLE IF EXISTS "tbl_address" CASCADE;
DROP TABLE IF EXISTS "tbl_user" CASCADE;
DROP TABLE IF EXISTS "tbl_token" CASCADE;

DROP TABLE IF EXISTS "tbl_question_option" CASCADE;
DROP TABLE IF EXISTS "tbl_question" CASCADE;

-- ==============================================================================
-- 2. TẠO CÁC BẢNG CỐT LÕI (Core & Auth Tối Giản)
-- ==============================================================================

-- Bảng User (Tích hợp luôn phân quyền Role)
CREATE TABLE "tbl_user" (
                            "id" BIGSERIAL PRIMARY KEY,
                            "first_name" VARCHAR(255),
                            "last_name" VARCHAR(255),
                            "gender" VARCHAR(255),
                            "date_of_birth" DATE,
                            "username" VARCHAR(255) UNIQUE NOT NULL,
                            "phone" VARCHAR(15),
                            "email" VARCHAR(255) UNIQUE NOT NULL,
                            "password" VARCHAR(255) NOT NULL,
                            "role" VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
                            "status" VARCHAR(50) DEFAULT 'ACTIVE',
                            "otp_code" VARCHAR(10),
                            "otp_expiry_time" TIMESTAMP,
                            "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "tbl_address" (
                               "id" BIGSERIAL PRIMARY KEY,
                               "street" VARCHAR(255),
                               "city" VARCHAR(255),
                               "country" VARCHAR(255),
                               "building" VARCHAR(255),
                               "address_type" INTEGER,
                               "user_id" BIGINT UNIQUE,
                               "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT "fk_user_address" FOREIGN KEY ("user_id") REFERENCES "tbl_user"("id") ON DELETE CASCADE
);

CREATE TABLE "tbl_token" (
                             "id" BIGSERIAL PRIMARY KEY,
                             "username" VARCHAR(255) NOT NULL,
                             "access_token" TEXT,
                             "refresh_token" TEXT,
                             "reset_token" TEXT,
                             "device_info" VARCHAR(255),
                             "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 3. TẠO CÁC BẢNG LMS PROFILE (Tách Student & Teacher)
-- ==============================================================================

-- Hồ sơ Sinh viên (Gắn 1-1 với tbl_user)
CREATE TABLE "tbl_student" (
                               "user_id" BIGINT PRIMARY KEY,
                               "student_code" VARCHAR(20) UNIQUE NOT NULL,
                               CONSTRAINT "fk_student_user" FOREIGN KEY ("user_id") REFERENCES "tbl_user"("id") ON DELETE CASCADE
);

-- Hồ sơ Giảng viên (Gắn 1-1 với tbl_user)
CREATE TABLE "tbl_teacher" (
                               "user_id" BIGINT PRIMARY KEY,
                               "department" VARCHAR(255),
                               CONSTRAINT "fk_teacher_user" FOREIGN KEY ("user_id") REFERENCES "tbl_user"("id") ON DELETE CASCADE
);

-- ==============================================================================
-- 4. TẠO CÁC BẢNG QUẢN LÝ LỚP HỌC (MyOpenMath Core)
-- ==============================================================================

-- Bảng Học kỳ
CREATE TABLE "tbl_semester" (
                                "id" BIGSERIAL PRIMARY KEY,
                                "name" VARCHAR(100) NOT NULL,
                                "start_date" DATE NOT NULL,
                                "end_date" DATE NOT NULL,
                                "is_active" BOOLEAN DEFAULT FALSE
);

-- Bảng Lớp học
CREATE TABLE "tbl_course" (
                              "id" BIGSERIAL PRIMARY KEY,
                              "instructor_id" BIGINT NOT NULL,
                              "semester_id" BIGINT NOT NULL,
                              "course_name" VARCHAR(255) NOT NULL,
                              "description" TEXT,
                              "join_code" VARCHAR(10) UNIQUE NOT NULL,
                              "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT "fk_course_instructor" FOREIGN KEY ("instructor_id") REFERENCES "tbl_teacher"("user_id") ON DELETE CASCADE,
                              CONSTRAINT "fk_course_semester" FOREIGN KEY ("semester_id") REFERENCES "tbl_semester"("id") ON DELETE CASCADE
);

-- Bảng Ghi danh (Sinh viên tham gia Lớp học)
CREATE TABLE "tbl_enrollment" (
                                  "id" BIGSERIAL PRIMARY KEY,
                                  "student_id" BIGINT NOT NULL,
                                  "course_id" BIGINT NOT NULL,
                                  "status" VARCHAR(20) DEFAULT 'ACTIVE',
                                  "enrolled_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT "fk_enrollment_student" FOREIGN KEY ("student_id") REFERENCES "tbl_student"("user_id") ON DELETE CASCADE,
                                  CONSTRAINT "fk_enrollment_course" FOREIGN KEY ("course_id") REFERENCES "tbl_course"("id") ON DELETE CASCADE,
                                  UNIQUE("student_id", "course_id") -- Một sinh viên chỉ được ghi danh 1 lớp 1 lần
);

-- Bảng Đánh giá Môn học
CREATE TABLE "tbl_rating" (
                              "id" BIGSERIAL PRIMARY KEY,
                              "student_id" BIGINT NOT NULL,
                              "course_id" BIGINT NOT NULL,
                              "score" SMALLINT NOT NULL CHECK (score >= 1 AND score <= 5),
                              "comment" TEXT,
                              "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT "fk_rating_student" FOREIGN KEY ("student_id") REFERENCES "tbl_student"("user_id") ON DELETE CASCADE,
                              CONSTRAINT "fk_rating_course" FOREIGN KEY ("course_id") REFERENCES "tbl_course"("id") ON DELETE CASCADE,
                              UNIQUE("student_id", "course_id") -- Một sinh viên chỉ được đánh giá 1 lớp 1 lần
);

-- ==============================================================================
-- 5. TẠO CÁC BẢNG CẤU TRÚC HỌC LIỆU
-- ==============================================================================

-- Bảng Chương học (Tuần học)
CREATE TABLE "tbl_module" (
                              "id" BIGSERIAL PRIMARY KEY,
                              "course_id" BIGINT NOT NULL,
                              "title" VARCHAR(255) NOT NULL,
                              "order_index" INTEGER DEFAULT 0,
                              CONSTRAINT "fk_module_course" FOREIGN KEY ("course_id") REFERENCES "tbl_course"("id") ON DELETE CASCADE
);

-- Bảng Tài liệu
CREATE TABLE "tbl_material" (
                                "id" BIGSERIAL PRIMARY KEY,
                                "title" VARCHAR(255) NOT NULL,
                                "type" VARCHAR(50) NOT NULL,
                                "content_url" TEXT NOT NULL,
                                "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Nối (Many-to-Many): 1 Material dùng cho nhiều Module
CREATE TABLE "tbl_module_material" (
                                       "module_id" BIGINT NOT NULL,
                                       "material_id" BIGINT NOT NULL,
                                       PRIMARY KEY ("module_id", "material_id"),
                                       CONSTRAINT "fk_mm_module" FOREIGN KEY ("module_id") REFERENCES "tbl_module"("id") ON DELETE CASCADE,
                                       CONSTRAINT "fk_mm_material" FOREIGN KEY ("material_id") REFERENCES "tbl_material"("id") ON DELETE CASCADE
);

-- ==============================================================================
-- 6. TẠO CÁC BẢNG BÀI TẬP VÀ CHẤM ĐIỂM (Assessment Engine)
-- ==============================================================================

-- Bảng Bộ bài tập / Quiz
CREATE TABLE "tbl_problem_set" (
                                   "id" BIGSERIAL PRIMARY KEY,
                                   "module_id" BIGINT NOT NULL,
                                   "title" VARCHAR(255) NOT NULL,
                                   "description" TEXT,
                                   "due_date" TIMESTAMP NOT NULL,
                                   "time_limit_mins" INTEGER,
                                   CONSTRAINT "fk_problemset_module" FOREIGN KEY ("module_id") REFERENCES "tbl_module"("id") ON DELETE CASCADE
);

-- Bảng Ngân hàng câu hỏi
CREATE TABLE "tbl_question" (
                                "id" BIGSERIAL PRIMARY KEY,
                                "problem_set_id" BIGINT NOT NULL,
                                "content" TEXT NOT NULL,
                                "type" VARCHAR(50) NOT NULL,
                                "correct_ans" TEXT NOT NULL,
                                "points" DOUBLE PRECISION DEFAULT 1.0,
                                CONSTRAINT "fk_question_problemset" FOREIGN KEY ("problem_set_id") REFERENCES "tbl_problem_set"("id") ON DELETE CASCADE
);

-- Bảng Lượt nộp bài
CREATE TABLE "tbl_submission" (
                                  "id" BIGSERIAL PRIMARY KEY,
                                  "student_id" BIGINT NOT NULL,
                                  "problem_set_id" BIGINT NOT NULL,
                                  "submitted_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  "total_score" DOUBLE PRECISION,
                                  CONSTRAINT "fk_submission_student" FOREIGN KEY ("student_id") REFERENCES "tbl_student"("user_id") ON DELETE CASCADE,
                                  CONSTRAINT "fk_submission_problemset" FOREIGN KEY ("problem_set_id") REFERENCES "tbl_problem_set"("id") ON DELETE CASCADE
);

-- Bảng Chi tiết câu trả lời của sinh viên
CREATE TABLE "tbl_student_answer" (
                                      "id" BIGSERIAL PRIMARY KEY,
                                      "submission_id" BIGINT NOT NULL,
                                      "question_id" BIGINT NOT NULL,
                                      "student_response" TEXT,
                                      "is_correct" BOOLEAN,
                                      "earned_points" DOUBLE PRECISION,
                                      CONSTRAINT "fk_answer_submission" FOREIGN KEY ("submission_id") REFERENCES "tbl_submission"("id") ON DELETE CASCADE,
                                      CONSTRAINT "fk_answer_question" FOREIGN KEY ("question_id") REFERENCES "tbl_question"("id") ON DELETE CASCADE
);

CREATE TABLE "tbl_question_option" (
                                       "question_id" BIGINT NOT NULL,
                                       "option_text" VARCHAR(255),
                                       CONSTRAINT "fk_question_option"
                                           FOREIGN KEY ("question_id")
                                               REFERENCES "tbl_question" ("id")
                                               ON DELETE CASCADE
);

-- ==============================================================================
-- 7. IMPORT DỮ LIỆU MẪU (Mock Data)
-- ==============================================================================

-- Insert 3 Users với Role trực tiếp
INSERT INTO "tbl_user" ("first_name", "last_name", "gender", "date_of_birth", "username", "phone", "email", "password", "role", "status") VALUES
                                                                                                                                              ('Kiet', 'Pham Tuan', 'MALE', '2004-05-15', 'kietpham_admin', '0859150559', 'phamkiet173@gmail.com', '$2a$10$YZBxFndNWcYtiAm4WI.kK.SG3q/xSebIFYfHy30ov/atSfU9Hr8Mi', 'ADMIN', 'ACTIVE'),
                                                                                                                                              ('Anh', 'Nguyen Mai', 'FEMALE', '1998-10-22', 'anh_nguyen', '0901234567', 'anh.nguyen@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'TEACHER', 'ACTIVE'),
                                                                                                                                              ('Bao', 'Tran Quoc', 'MALE', '1995-02-28', 'bao_tran', '0912345678', 'bao.tran@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'STUDENT', 'ACTIVE');

-- ID 2 là Giảng viên (TEACHER)
INSERT INTO "tbl_teacher" ("user_id", "department") VALUES
    (2, 'Khoa Cong nghe Thong tin');

-- ID 3 là Sinh viên (STUDENT)
INSERT INTO "tbl_student" ("user_id", "student_code") VALUES
    (3, 'ITCSIU22235');

-- Thêm 1 Học kỳ mẫu
INSERT INTO "tbl_semester" ("name", "start_date", "end_date", "is_active") VALUES
    ('HK2 2025-2026', '2026-02-01', '2026-06-30', TRUE);

-- Thêm 1 Khóa học mẫu do Giảng viên Anh (ID 2) tạo
INSERT INTO "tbl_course" ("instructor_id", "semester_id", "course_name", "description", "join_code") VALUES
    (2, 1, 'Data Structures and Algorithms', 'Mon hoc CTDL & TT', 'DSA2026X');

-- Ghi danh Sinh viên Bao (ID 3) vào lớp
INSERT INTO "tbl_enrollment" ("student_id", "course_id") VALUES
    (3, 1);