-- ==============================================================================
-- 1. DỌN DẸP BẢNG CŨ (Sử dụng CASCADE để tự động xóa các khóa ngoại liên quan)
-- ==============================================================================
DROP TABLE IF EXISTS "tbl_role_has_permission" CASCADE;
DROP TABLE IF EXISTS "tbl_user_has_role" CASCADE;
DROP TABLE IF EXISTS "tbl_group_has_user" CASCADE;
DROP TABLE IF EXISTS "tbl_group" CASCADE;
DROP TABLE IF EXISTS "tbl_permission" CASCADE;
DROP TABLE IF EXISTS "tbl_role" CASCADE;
DROP TABLE IF EXISTS "tbl_address" CASCADE;
DROP TABLE IF EXISTS "tbl_user" CASCADE;
DROP TABLE IF EXISTS "tbl_token" CASCADE;

-- ==============================================================================
-- 2. TẠO CÁC BẢNG CỐT LÕI VÀ PHÂN QUYỀN (PostgreSQL chuẩn)
-- ==============================================================================

-- Bảng User
CREATE TABLE "tbl_user" (
                            "id" BIGSERIAL PRIMARY KEY,
                            "first_name" VARCHAR(255),
                            "last_name" VARCHAR(255),
                            "gender" VARCHAR(255),
                            "date_of_birth" DATE,
                            "username" VARCHAR(255) UNIQUE NOT NULL,
                            "phone" VARCHAR(15),
                            "email" VARCHAR(255),
                            "password" VARCHAR(255),
                            "user_type" VARCHAR(255),
                            "status" VARCHAR(255),
                            "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Address (Quan hệ 1-1 với User)
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

-- Bảng Role
CREATE TABLE "tbl_role" (
                            "id" BIGSERIAL PRIMARY KEY,
                            "name" VARCHAR(255) UNIQUE NOT NULL,
                            "description" VARCHAR(255),
                            "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Permission
CREATE TABLE "tbl_permission" (
                                  "id" BIGSERIAL PRIMARY KEY,
                                  "name" VARCHAR(255) UNIQUE NOT NULL,
                                  "url" VARCHAR(255),
                                  "description" VARCHAR(255),
                                  "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Group
CREATE TABLE "tbl_group" (
                             "id" BIGSERIAL PRIMARY KEY,
                             "name" VARCHAR(255),
                             "role_id" BIGINT,
                             CONSTRAINT "fk_group_role" FOREIGN KEY ("role_id") REFERENCES "tbl_role" ("id") ON DELETE SET NULL
);

-- Các bảng trung gian (Many-to-Many)
CREATE TABLE "tbl_group_has_user" (
                                      "group_id" BIGINT,
                                      "user_id" BIGINT,
                                      PRIMARY KEY ("group_id", "user_id"),
                                      CONSTRAINT "fk_ghu_group" FOREIGN KEY ("group_id") REFERENCES "tbl_group" ("id") ON DELETE CASCADE,
                                      CONSTRAINT "fk_ghu_user" FOREIGN KEY ("user_id") REFERENCES "tbl_user" ("id") ON DELETE CASCADE
);

CREATE TABLE "tbl_user_has_role" (
                                     "user_id" BIGINT,
                                     "role_id" BIGINT,
                                     PRIMARY KEY ("user_id", "role_id"),
                                     CONSTRAINT "fk_uhr_user" FOREIGN KEY ("user_id") REFERENCES "tbl_user" ("id") ON DELETE CASCADE,
                                     CONSTRAINT "fk_uhr_role" FOREIGN KEY ("role_id") REFERENCES "tbl_role" ("id") ON DELETE CASCADE
);

CREATE TABLE "tbl_role_has_permission" (
                                           "role_id" BIGINT,
                                           "permission_id" BIGINT,
                                           PRIMARY KEY ("role_id", "permission_id"),
                                           CONSTRAINT "fk_rhp_role" FOREIGN KEY ("role_id") REFERENCES "tbl_role" ("id") ON DELETE CASCADE,
                                           CONSTRAINT "fk_rhp_permission" FOREIGN KEY ("permission_id") REFERENCES "tbl_permission" ("id") ON DELETE CASCADE
);

-- Bảng Token (Quan hệ N-1 với User: 1 user có thể đăng nhập trên nhiều thiết bị)
-- Bảng Token
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
-- 3. IMPORT DỮ LIỆU MẪU (Mock Data)
-- ==============================================================================

-- Thêm 3 Role cơ bản
INSERT INTO "tbl_role" ("name", "description") VALUES
                                                   ('OWNER', 'Quản trị viên cấp cao nhất'),
                                                   ('ADMIN', 'Quản trị viên hệ thống'),
                                                   ('USER', 'Người dùng thông thường');

-- Thêm 30 User (Mật khẩu mặc định là 123456 đã được mã hóa BCrypt)
INSERT INTO "tbl_user" ("first_name", "last_name", "gender", "date_of_birth", "username", "phone", "email", "password", "user_type", "status") VALUES
                                                                                                                                                   ('Kiet', 'Pham Tuan', 'MALE', '2004-05-15', 'kietpham_admin', '0859150559', 'phamkiet173@gmail.com', '$2a$10$YZBxFndNWcYtiAm4WI.kK.SG3q/xSebIFYfHy30ov/atSfU9Hr8Mi', 'OWNER', 'ACTIVE'),
                                                                                                                                                   ('Anh', 'Nguyen Mai', 'FEMALE', '1998-10-22', 'anh_nguyen', '0901234567', 'anh.nguyen@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'ADMIN', 'ACTIVE'),
                                                                                                                                                   ('Bao', 'Tran Quoc', 'MALE', '1995-02-28', 'bao_tran', '0912345678', 'bao.tran@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'ADMIN', 'INACTIVE'),
                                                                                                                                                   ('Linh', 'Hoang Thuy', 'FEMALE', '2001-12-05', 'linh_hoang', '0923456789', 'linh.hoang@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Minh', 'Le Hai', 'OTHER', '1990-08-14', 'minh_le', '0934567890', 'minh.le@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'NONE'),
                                                                                                                                                   ('Hoa', 'Vu Thi', 'FEMALE', '1985-04-30', 'hoa_vu', '0945678901', 'hoa.vu@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Cuong', 'Dang Viet', 'MALE', '1992-11-11', 'cuong_dang', '0956789012', 'cuong.dang@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'BANNED'),
                                                                                                                                                   ('Trang', 'Bui Thu', 'FEMALE', '1999-07-07', 'trang_bui', '0967890123', 'trang.bui@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Dat', 'Do Tien', 'MALE', '2000-09-09', 'dat_do', '0978901234', 'dat.do@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Hanh', 'Vo My', 'FEMALE', '1994-03-15', 'hanh_vo', '0989012345', 'hanh.vo@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'INACTIVE'),
                                                                                                                                                   ('Khoa', 'Nguyen Dang', 'MALE', '1997-06-20', 'khoa_nguyen', '0990123456', 'khoa.nguyen@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Lan', 'Tran Ngoc', 'FEMALE', '1988-12-25', 'lan_tran', '0909123456', 'lan.tran@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'NONE'),
                                                                                                                                                   ('Phuc', 'Le Hoang', 'MALE', '2002-01-10', 'phuc_le', '0918234567', 'phuc.le@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Yen', 'Pham Hai', 'FEMALE', '1996-05-05', 'yen_pham', '0927345678', 'yen.pham@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Son', 'Vu Truong', 'MALE', '1993-08-18', 'son_vu', '0936456789', 'son.vu@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'BANNED'),
                                                                                                                                                   ('Nhung', 'Dang Hong', 'FEMALE', '1989-02-14', 'nhung_dang', '0945567890', 'nhung.dang@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Tuan', 'Bui Anh', 'MALE', '1991-10-10', 'tuan_bui', '0954678901', 'tuan.bui@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'INACTIVE'),
                                                                                                                                                   ('Thao', 'Do Thanh', 'FEMALE', '1995-09-22', 'thao_do', '0963789012', 'thao.do@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Thanh', 'Vo Van', 'MALE', '1987-07-01', 'thanh_vo', '0972890123', 'thanh.vo@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'NONE'),
                                                                                                                                                   ('Mai', 'Hoang Nhat', 'FEMALE', '2000-04-04', 'mai_hoang', '0981901234', 'mai.hoang@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Hung', 'Nguyen Manh', 'MALE', '1998-11-20', 'hung_nguyen', '0990012345', 'hung.nguyen@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Quynh', 'Tran Truc', 'FEMALE', '1992-06-06', 'quynh_tran', '0908123456', 'quynh.tran@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Phong', 'Le Tuan', 'MALE', '1986-03-08', 'phong_le', '0917234567', 'phong.le@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'BANNED'),
                                                                                                                                                   ('Diep', 'Pham Ngoc', 'FEMALE', '1999-12-31', 'diep_pham', '0926345678', 'diep.pham@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Nghia', 'Vu Trong', 'MALE', '1994-01-25', 'nghia_vu', '0935456789', 'nghia.vu@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'INACTIVE'),
                                                                                                                                                   ('My', 'Dang Tra', 'FEMALE', '1997-08-08', 'my_dang', '0944567890', 'my.dang@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Vinh', 'Bui Quang', 'MALE', '2003-02-18', 'vinh_bui', '0953678901', 'vinh.bui@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Ngoc', 'Do Bich', 'FEMALE', '1990-10-30', 'ngoc_do', '0962789012', 'ngoc.do@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'NONE'),
                                                                                                                                                   ('Huy', 'Vo Quang', 'MALE', '1996-05-12', 'huy_vo', '0971890123', 'huy.vo@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE'),
                                                                                                                                                   ('Phuong', 'Hoang Minh', 'OTHER', '1991-07-15', 'phuong_hoang', '0980901234', 'phuong.hoang@example.com', '$2a$10$W2neF9.6Agi6kAKVq8q3fec5dHW8KUA.b0VSIGdIZyUervbe.Q7Fq', 'USER', 'ACTIVE');

-- Thêm 30 Address tương ứng cho 30 User
INSERT INTO "tbl_address" ("street", "city", "country", "building", "address_type", "user_id") VALUES
                                                                                                   ('Khu pho 6, P. Linh Trung', 'Ho Chi Minh', 'Vietnam', 'Dai hoc Quoc te - ĐHQG HCM', 1, 1),
                                                                                                   ('123 Le Loi', 'Bien Hoa', 'Vietnam', 'Chung cu A', 0, 2),
                                                                                                   ('456 Nguyen Trai', 'Ha Noi', 'Vietnam', 'Toa nha B', 1, 3),
                                                                                                   ('789 Tran Hung Dao', 'Da Nang', 'Vietnam', 'Azura Tower', 2, 4),
                                                                                                   ('101 Le Van Sy', 'Ho Chi Minh', 'Vietnam', 'Nha rieng', 0, 5),
                                                                                                   ('202 Dien Bien Phu', 'Hai Phong', 'Vietnam', 'Toa nha Cat Bi', 1, 6),
                                                                                                   ('303 Nguyen Thi Minh Khai', 'Nha Trang', 'Vietnam', 'Gold Coast', 0, 7),
                                                                                                   ('404 Pham Van Dong', 'Da Lat', 'Vietnam', 'Biet thu C', 2, 8),
                                                                                                   ('505 Cach Mang Thang 8', 'Can Tho', 'Vietnam', 'Vincom Xuan Khanh', 1, 9),
                                                                                                   ('606 Hai Ba Trung', 'Hue', 'Vietnam', 'Nha rieng', 0, 10),
                                                                                                   ('707 Ba Trieu', 'Ha Noi', 'Vietnam', 'Vincom Center', 1, 11),
                                                                                                   ('808 Nguyen Dinh Chieu', 'Ho Chi Minh', 'Vietnam', 'Chung cu D', 0, 12),
                                                                                                   ('909 Ly Thuong Kiet', 'Vung Tau', 'Vietnam', 'Lapen Center', 2, 13),
                                                                                                   ('111 Vo Van Tan', 'Da Nang', 'Vietnam', 'Nha rieng', 0, 14),
                                                                                                   ('222 Pasteur', 'Ho Chi Minh', 'Vietnam', 'Toa nha E', 1, 15),
                                                                                                   ('333 Dong Khoi', 'Bien Hoa', 'Vietnam', 'Pegasus Plaza', 1, 16),
                                                                                                   ('444 Ton Duc Thang', 'Ha Noi', 'Vietnam', 'Nha rieng', 0, 17),
                                                                                                   ('555 Nguyen Hue', 'Ho Chi Minh', 'Vietnam', 'Bitexco', 2, 18),
                                                                                                   ('666 Le Duan', 'Da Nang', 'Vietnam', 'Indochina Riverside', 1, 19),
                                                                                                   ('777 Tran Phu', 'Nha Trang', 'Vietnam', 'Nha rieng', 0, 20),
                                                                                                   ('888 Hung Vuong', 'Can Tho', 'Vietnam', 'Chung cu F', 0, 21),
                                                                                                   ('999 Quang Trung', 'Hai Phong', 'Vietnam', 'Toa nha G', 1, 22),
                                                                                                   ('12 Nguyen Thi Thap', 'Ho Chi Minh', 'Vietnam', 'Sunrise City', 2, 23),
                                                                                                   ('34 Phan Dang Luu', 'Ha Noi', 'Vietnam', 'Nha rieng', 0, 24),
                                                                                                   ('56 Hoang Van Thu', 'Da Lat', 'Vietnam', 'Chung cu H', 0, 25),
                                                                                                   ('78 Truong Chinh', 'Hue', 'Vietnam', 'Toa nha I', 1, 26),
                                                                                                   ('90 Cong Hoa', 'Ho Chi Minh', 'Vietnam', 'Pico Plaza', 1, 27),
                                                                                                   ('23 Nguyen Van Cu', 'Can Tho', 'Vietnam', 'Nha rieng', 0, 28),
                                                                                                   ('45 Le Hong Phong', 'Vung Tau', 'Vietnam', 'Imperial Hotel', 2, 29),
                                                                                                   ('67 Xo Viet Nghe Tinh', 'Ho Chi Minh', 'Vietnam', 'Landmark 81', 1, 30);