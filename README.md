# Java FTP Client

A Java-based FTP client with a graphical user interface, supporting file and directory operations, upload, and download.

## Features

- Connect to FTP servers using username and password authentication
- Browse and manage remote directories and files
- Upload and download files and directories
- Create new directories and delete existing ones
- Rename files and directories
- Refresh the file list to reflect changes on the server
- Cross-platform support (Windows, Linux)
- Intuitive graphical user interface (GUI) built with Java Swing

## Requirements

- Java 17 or later
- Maven 3.6 or later (for building and dependencies management)

## Building and Running

1. Clone the repository
2. Navigate to the project directory: `cd ftpclient`
3. Build the project using Maven: `mvn clean package`
4. Run the application: `java -jar target/ftpclient-1.0-SNAPSHOT.jar`

## Usage

1. Launch the application and enter the FTP server address, port, username, and password.
2. Click the "Connect" button to establish a connection to the FTP server.
3. Once connected, you can browse and manage remote directories and files using the file explorer component.
4. Use the toolbar buttons or right-click menu to perform file and directory operations like uploading, downloading,
   renaming, and deleting.
5. The refresh button updates the view to reflect changes made on the FTP server.

## System Design

### Key Modules

- **FTP Client Module**: Manages FTP connection, authentication, and file transfer operations.
- **User Interface Module**: Provides a graphical user interface (GUI) using Java Swing.
- **Virtual File System Module**: Abstracts the file system structure to facilitate browsing and manipulation of files
  and directories on both the local system and the FTP server.
- **Utility Module**: Contains helper functions for file I/O, logging, time formatting, and icon loading.

### Development Environment

- **Operating System**: Windows 10/11, Linux (tested on multiple distributions)
- **JDK Version**: OpenJDK 17.0.10
- **IDE**: IntelliJ IDEA or any preferred Java IDE

## System Architecture

The system uses a modular, object-oriented design to separate concerns and ensure easy maintenance and scalability:

- **FTP Protocol Implementation**: Handles communication between the client and the FTP server using FTP commands such
  as `USER`, `PASS`, `LIST`, `RETR`, `STOR`.
- **Graphical User Interface (GUI)**: Built using Java Swing, it provides intuitive file and directory management
  through a file explorer-style interface.
- **File System Abstraction**: Local and remote file systems are abstracted to simplify file operations, allowing
  seamless interaction with both.

## Functional Requirements

1. **Connect to FTP Servers**: Establish a connection using server address, port, username, and password.
2. **File Management**: Browse, upload, download, rename, and delete files or directories.
3. **Create New Directories**: Users can create new folders on the FTP server.
4. **Display File Information**: Files and directories should display key details such as name, size, and modification
   date.
5. **Refresh Functionality**: The client can refresh the file list to reflect recent changes on the server.

### Performance and System Requirements

- **CPU**: Dual-core 1.5 GHz or higher
- **RAM**: 1 GB minimum
- **Disk Space**: 200 MB for installation (excluding Java Runtime Environment)
- **Java Runtime Environment (JRE)**: Version 8 or later

### Known Limitations

- Basic error handling is implemented but can be improved for network failures and file permission issues.
- The UI is functional but can be further enhanced for a more modern look and feel.
- Large file transfers may experience reduced performance depending on network speed and server capacity.

## Testing

The system was manually tested and passed all test cases, including connection, file operations (upload, download,
delete, rename), and directory creation.

### Test Cases

1. **Connection Test**: Verifies the ability to connect to FTP servers with valid credentials.
2. **File Upload/Download Test**: Ensures correct file transfer between the client and the server.
3. **Directory Operations Test**: Tests creating, deleting, and renaming directories.
4. **File Information Display**: Validates that file properties (name, size, modification date) are correctly displayed.

## Dependencies

- Apache Commons Lang
- Apache Commons IO
- Google Guava
- Log4j
- PrettyTime (for relative time formatting)
- Apache Batik (for SVG icon rendering)

## Future Improvements

1. **Modernize the User Interface**: Enhance the GUI to have a more modern look with custom themes and styling.
2. **Error Handling**: Improve error handling, particularly in cases of network disruptions and file permissions.
3. **Advanced Features**: Add support for resume on file transfer interruption, file synchronization, and batch
   processing.

## Contributing

Contributions are welcome! Please open an issue for bug reports or feature requests. When submitting pull requests,
ensure that you follow standard coding practices and provide appropriate documentation for any new features or changes.

## License

This project is a personal graduation design and is provided for reference and educational purposes only. It is not
intended for commercial use. The author assumes no responsibility for any issues, damages, or liabilities that may arise
from using, modifying, or distributing this software. Use at your own risk.

## Acknowledgments

This project utilizes the following open-source libraries:

- Apache Commons Lang
- Google Guava
- Apache Commons IO
- Log4j
- PrettyTime
- Apache Batik

Special thanks to the contributors and maintainers of these libraries for their efforts.
