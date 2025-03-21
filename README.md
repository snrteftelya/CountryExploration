# Country Search API

## Описание
Country Search API — это RESTful API на базе Spring Boot, которое позволяет управлять данными о странах, городах и нациях.

## Функциональность
- Управление странами (CRUD-операции)
- Управление городами (CRUD-операции)
- Управление нациями (CRUD-операции)
- Поиск данных по названию
- Связи между странами, городами и нациями

## Технологии
- Java 8+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Hibernate
- Lombok
- Swagger

## Установка и запуск

### 1. Клонирование репозитория
```sh
git clone https://github.com/snrteftelya/CountryExploration.git
cd CountryExploration
```

### 2. Конфигурация базы данных
Создайте файл `.env` в корне проекта

### 3. Запуск приложения

#### С использованием Maven
```sh
./mvnw spring-boot:run
```

## API эндпоинты

### 1. Страны
- `GET /api/country` — получить все страны
- `GET /api/country/{id}` — получить страну по ID
- `POST /api/country` — добавить новую страну
- `PUT /api/country/{id}` — обновить страну
- `DELETE /api/country/{id}` — удалить страну

### 2. Города
- `GET /api/cities` — получить все города
- `GET /api/countries/{countryId}/cities` — получить города в стране
- `POST /api/countries/{countryId}/cities` — добавить город в страну
- `PUT /api/cities/{id}` — обновить город
- `DELETE /api/countries/{countryId}/cities` — удалить города в стране

### 3. Нации
- `GET /api/nations` — получить все нации
- `GET /api/nations/{nationId}/countries` — получить страны нации
- `POST /api/countries/{countryId}/nations` — добавить нацию в страну
- `PUT /api/nations/{id}` — обновить нацию
- `DELETE /api/nations/{id}` — удалить нацию
- `DELETE /api/countries/{countryId}/nations/{nationId}` — удалить нацию из страны

## Контакты
Разработчик: **snrteftelya**  
GitHub: [https://github.com/snrteftelya](https://github.com/snrteftelya)
Sonar: [https://sonarcloud.io/project/overview?id=snrteftelya_CountryExploration](https://sonarcloud.io/project/overview?id=snrteftelya_CountryExploration)