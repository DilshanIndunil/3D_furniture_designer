# Furniture Designer Application

A JavaFX desktop application for furniture design visualization, allowing designers to create and visualize room layouts in both 2D and 3D.

## Features

- User authentication for designers
- Room dimension input and customization
- 2D and 3D visualization of room layouts
- Furniture placement and customization
- Color scheme selection
- Save and load designs
- Modern and intuitive user interface

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Dependencies

- JavaFX 17.0.2
- SQLite JDBC 3.42.0.0
- JMonkeyEngine 3.6.0-stable
- Gson 2.10.1

## Setup

1. Clone the repository:

```bash
git clone <repository-url>
cd furniture-designer
```

2. Build the project:

```bash
mvn clean install
```

3. Run the application:

```bash
mvn javafx:run
```

## Default Login

- Username: designer
- Password: design123

## Usage

1. Log in using the default credentials
2. Create a new design by entering room dimensions
3. Add furniture using the furniture panel
4. Customize colors and layout
5. Switch between 2D and 3D views
6. Save your design for later use

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── furnituredesign/
│   │           ├── controllers/
│   │           ├── models/
│   │           ├── services/
│   │           └── Main.java
│   └── resources/
│       ├── fxml/
│       └── styles/
└── test/
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
