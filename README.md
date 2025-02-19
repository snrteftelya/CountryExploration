# 🌍 Country Exploration API

### 📌 Описание
**Country Exploration API** — это простой REST-сервис, написанный на **Spring Boot**, который предоставляет информацию о странах.  
Позволяет получать список стран, фильтровать их по имени и столице, а также запрашивать данные по `id`.

---

## 🚀 Установка и запуск

### 🔹 1. Системные требования
- **Java 17+** (убедитесь, что установлена последняя версия: `java -version`)
- **Maven 3.8+** (проверьте: `mvn -version`)

### 🔹 2. Склонировать проект
```sh
git clone https://github.com/snrteftelya/CountryExploration.git
cd CountryExploration
```

### 🔹 3. Запустить проект

#### 🏗 Сборка проекта
Перед первым запуском установите зависимости:
```sh
mvn clean install
```

#### 🌍 Запуск сервиса
```sh
mvn spring-boot:run
```
После успешного запуска API будет доступно по адресу:  
🔗 **http://localhost:8080**

---

## 📡 API Эндпоинты

### 🔹 Получить список всех стран
```http
GET /api/countries
```
📌 **Пример ответа:**
```json
[
    {
        "id": 1,
        "name": "USA",
        "capital": "Washington",
        "population": 331000000
    },
    {
        "id": 2,
        "name": "Canada",
        "capital": "Ottawa",
        "population": 38000000
    }
]
```

### 🔹 Поиск стран по имени или столице
```http
GET /api/countries?name=Canada
GET /api/countries?capital=Berlin
```

📌 **Пример ответа:**
```json
[
    {
        "id": 2,
        "name": "Canada",
        "capital": "Ottawa",
        "population": 38000000
    }
]
```

### 🔹 Получить страну по `id`
```http
GET /api/countries/1
```
📌 **Пример ответа:**
```json
{
    "id": 1,
    "name": "USA",
    "capital": "Washington",
    "population": 331000000
}
```

---

## 🔧 Возможные ошибки

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `java: command not found` | Java не установлена | Установите Java 17+ |
| `mvn: command not found` | Maven не установлен | Установите Maven 3.8+ |
| `Address already in use` | Порт 8080 занят | Запустите на другом порту: `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"` |

---

## Результаты SonarCloud
https://sonarcloud.io/project/overview?id=snrteftelya_CountryExploration