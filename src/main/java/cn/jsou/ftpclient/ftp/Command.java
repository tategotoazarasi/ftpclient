package cn.jsou.ftpclient.ftp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.jsou.ftpclient.ftp.ReplyCode.*;

public enum Command {
	/**
	 * 用户名
	 *
	 * <p>参数字段是一个Telnet字符串，用于识别用户。用户标识是服务器为访问其文件系统所需的。
	 * 这个命令通常是用户在控制连接建立后传输的第一个命令（有些服务器可能要求这样）。一些服务器还可能要求额外的身份信息，如密码和/或账户命令。
	 * 服务器可能允许在任何时候输入新的USER命令，以更改访问控制和/或记账信息。这将清除已提供的任何用户、密码和账户信息，并重新开始登录序列。 所有传输参数保持不变，任何正在进行的文件传输都将在旧的访问控制参数下完成。</p>
	 */
	USER_NAME("USER",
	          new HashSet<>(Arrays.asList(USER_LOGGED_IN,
	                                      NOT_LOGGED_IN,
	                                      SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                      SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                      SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                      USER_NAME_OKAY_NEED_PASSWORD,
	                                      NEED_ACCOUNT_FOR_LOGIN))),
	/**
	 * 密码
	 *
	 * <p>参数字段是一个指定用户密码的Telnet字符串。这个命令必须紧跟在用户名命令之后，并且对于一些站点来说，完成了用户的访问控制身份验证。
	 * 由于密码信息非常敏感，通常希望“掩盖”或抑制打印出来。看来服务器没有确保这一点的万无一失的方法。 因此，隐藏敏感密码信息的责任在于用户-FTP进程。</p>
	 */
	PASSWORD("PASS",
	         new HashSet<>(List.of(USER_LOGGED_IN,
	                               COMMAND_NOT_IMPLEMENTED_SUPERFLUOUS,
	                               NOT_LOGGED_IN,
	                               SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                               SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                               BAD_SEQUENCE_OF_COMMANDS,
	                               SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                               NEED_ACCOUNT_FOR_LOGIN))),
	/**
	 * 账户
	 *
	 * <p>参数字段是一个Telnet字符串，用于识别用户的账户。这个命令不一定与USER命令相关，因为有些站点可能需要登录账户，
	 * 而其他站点只在特定访问时需要，如存储文件。在后一种情况下，命令可以在任何时候到达。
	 * 对于自动化，有回复代码来区分这些情况：当登录需要账户信息时，成功的PASSword命令的响应是回复代码332。另一方面，如果登录不需要账户信息，
	 * 成功的PASSword命令的回复是230；如果在对话中稍后发出的命令需要账户信息， 服务器应根据它是存储（等待接收ACCounT命令）还是丢弃命令，分别返回332或532回复。</p>
	 */
	ACCOUNT("ACCT", new HashSet<>(List.of(USER_LOGGED_IN,
	                                      COMMAND_NOT_IMPLEMENTED_SUPERFLUOUS,
	                                      NOT_LOGGED_IN,
	                                      SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                      SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                      BAD_SEQUENCE_OF_COMMANDS,
	                                      SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 更改工作目录
	 *
	 * <p>此命令允许用户在不更改其登录或记账信息的情况下，使用不同的目录或数据集进行文件存储或检索。传输参数同样未改变。
	 * 参数是指定目录或其他系统依赖的文件组指示符的路径名。</p>
	 */
	CHANGE_WORKING_DIRECTORY("CWD", new HashSet<>(List.of(REQUESTED_FILE_ACTION_OKAY,
	                                                      SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                                      SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                                      COMMAND_NOT_IMPLEMENTED,
	                                                      SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 更改到父目录
	 *
	 * <p>此命令是CWD的特例，包含在内是为了简化在具有不同父目录命名语法的操作系统之间传输目录树的程序的实现。
	 * 回复代码应与CWD的回复代码相同。有关更多详细信息，请参见附录II。</p>
	 */
	CHANGE_TO_PARENT_DIRECTORY("CDUP", new HashSet<>(List.of(COMMAND_OKAY, SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                                         SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                                         COMMAND_NOT_IMPLEMENTED,
	                                                         SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 结构挂载
	 *
	 * <p>此命令允许用户在不更改其登录或账户信息的情况下，挂载不同的文件系统数据结构。
	 * 传输参数同样未改变。参数是指定目录或其他系统依赖的文件组指示符的路径名。</p>
	 */
	STRUCTURE_MOUNT("SMNT",
	                new HashSet<>(List.of(COMMAND_NOT_IMPLEMENTED_SUPERFLUOUS, SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                      SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                      COMMAND_NOT_IMPLEMENTED,
	                                      SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 重新初始化
	 *
	 * <p>此命令终止一个USER会话，清除所有I/O和账户信息，但允许任何正在进行的传输完成。
	 * 所有参数重置为默认设置，控制连接保持开放。这与用户刚刚打开控制连接后发现的状态相同。预计之后会有一个USER命令。</p>
	 */
	REINITIALIZE("REIN",
	             new HashSet<>(List.of(SERVICE_READY_IN_MINUTES,
	                                   SERVICE_READY_FOR_NEW_USER,
	                                   SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                   SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                   COMMAND_NOT_IMPLEMENTED))),
	/**
	 * 注销
	 *
	 * <p>此命令终止一个USER会话，如果没有进行文件传输，服务器将关闭控制连接。如果文件传输正在进行中，连接将保持开放以等待结果响应，
	 * 然后服务器将其关闭。如果用户进程为多个用户传输文件但不希望为每个用户关闭然后重新打开连接，则应使用REIN命令而不是QUIT。 控制连接上的意外关闭将导致服务器采取中止（ABOR）和注销（QUIT）的有效操作。</p>
	 */
	LOGOUT("QUIT", new HashSet<>(List.of(SERVICE_CLOSING_CONTROL_CONNECTION, SYNTAX_ERROR_COMMAND_UNRECOGNIZED))),
	/**
	 * 数据端口
	 *
	 * <p>参数是用于数据连接的数据端口的HOST-PORT规范。用户和服务器数据端口都有默认值，在正常情况下不需要这个命令及其回复。
	 * 如果使用此命令，参数是32位互联网主机地址和16位TCP端口地址的连接。 这个地址信息被分解为8位字段，每个字段的值作为十进制数（以字符字符串表示）传输。字段之间用逗号分隔。一个端口命令将是： PORT
	 * h1,h2,h3,h4,p1,p2 其中h1是互联网主机地址的高8位。</p>
	 */
	DATA_PORT("PORT",
	          new HashSet<>(List.of(COMMAND_OKAY,
	                                SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                NOT_LOGGED_IN))),
	/**
	 * 被动
	 *
	 * <p>此命令请求服务器-DTP在一个数据端口（不是其默认数据端口）上“监听”，并等待连接而不是在收到传输命令时发起连接。
	 * 此命令的响应包括服务器正在监听的主机和端口地址。</p>
	 */
	PASSIVE("PASV", new HashSet<>(List.of(ENTERING_PASSIVE_MODE, SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                      SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, COMMAND_NOT_IMPLEMENTED,
	                                      SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                      NOT_LOGGED_IN))),
	/**
	 * 传输模式
	 *
	 * <p>参数是一个指定数据传输模式的单个Telnet字符代码，如传输模式部分所述。默认传输模式是流。</p>
	 */
	TRANSFER_MODE("MODE", new HashSet<>(List.of(COMMAND_OKAY,
	                                            SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                            SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                            COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
	                                            SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                            NOT_LOGGED_IN))),
	/**
	 * 表示类型
	 *
	 * <p>参数指定了在数据表示和存储部分描述的表示类型。一些类型需要第二个参数。第一个参数由单个Telnet字符表示，
	 * 就像ASCII和EBCDIC的第二个格式参数一样；本地字节的第二个参数是一个十进制整数，表示字节大小。参数之间用（空格，ASCII代码32）分隔。
	 * 默认表示类型是ASCII非打印。如果更改了格式参数，稍后仅更改第一个参数，格式则返回到非打印默认值。</p>
	 */
	REPRESENTATION_TYPE("TYPE", new HashSet<>(List.of(COMMAND_OKAY,
	                                                  SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                                  SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                                  COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
	                                                  SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                                  NOT_LOGGED_IN))),
	/**
	 * 文件结构
	 *
	 * <p>参数是一个指定文件结构的单个Telnet字符代码，如数据表示和存储部分所述。默认结构是文件。</p>
	 */
	FILE_STRUCTURE("STRU", new HashSet<>(List.of(COMMAND_OKAY,
	                                             SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                             SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                             COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
	                                             SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                             NOT_LOGGED_IN))),

	/**
	 * 分配
	 *
	 * <p>某些服务器可能要求此命令来预留足够的存储空间以容纳要传输的新文件。参数应该是一个十进制整数，
	 * 表示要为文件保留的字节（使用逻辑字节大小）的数量。对于带有记录或页面结构的文件，可能还需要最大记录或页面大小（以逻辑字节为单位）；
	 * 这由命令的第二个参数字段中的十进制整数表示。这第二个参数是可选的，但如果存在，应该用三个Telnet字符 R 与第一个参数分开。
	 * 此命令应该后跟一个STORe或APPEnd命令。对于那些不需要事先声明文件的最大大小的服务器，ALLO命令应该被视为NOOP（无操作），
	 * 而那些只对最大记录或页面大小感兴趣的服务器应该接受第一个参数中的虚拟值并忽略它。</p>
	 */
	ALLOCATE("ALLO",
	         new HashSet<>(List.of(COMMAND_OKAY, COMMAND_NOT_IMPLEMENTED_SUPERFLUOUS, SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                               SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
	                               SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                               NOT_LOGGED_IN))),
	/**
	 * 重启
	 *
	 * <p>参数字段代表要重新启动文件传输的服务器标记。此命令不会导致文件传输，而是跳过文件到指定的数据检查点。
	 * 此命令应立即后跟适当的FTP服务命令，该命令将导致文件传输恢复。</p>
	 */
	RESTART("REST", new HashSet<>(List.of(SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                      SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, COMMAND_NOT_IMPLEMENTED,
	                                      SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                      NOT_LOGGED_IN, REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION))),
	/**
	 * 存储
	 *
	 * <p>此命令使服务器-DTP接受通过数据连接传输的数据，并将数据作为文件存储在服务器站点。
	 * 如果路径名中指定的文件在服务器站点存在，则其内容将被传输的数据替换。如果路径名中指定的文件不存在，则将在服务器站点创建新文件。</p>
	 */
	STORE("STOR",
	      new HashSet<>(List.of(DATA_CONNECTION_ALREADY_OPEN,
	                            FILE_STATUS_OKAY,
	                            RESTART_MARKER_REPLY,
	                            CLOSING_DATA_CONNECTION,
	                            REQUESTED_FILE_ACTION_OKAY,
	                            CANT_OPEN_DATA_CONNECTION,
	                            CONNECTION_CLOSED_TRANSFER_ABORTED,
	                            REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING,
	                            REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
	                            REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE,
	                            NEED_ACCOUNT_FOR_STORING_FILES,
	                            REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                            REQUESTED_ACTION_NOT_TAKEN_INSUFFICIENT_STORAGE_SPACE,
	                            REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
	                            SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                            SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                            SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                            NOT_LOGGED_IN))),
	/**
	 * 存储唯一
	 *
	 * <p>此命令的行为类似于STOR，除了结果文件将在当前目录下以该目录唯一的名称创建。250传输开始响应必须包括生成的名称。</p>
	 */
	STORE_UNIQUE("STOU", new HashSet<>(List.of(DATA_CONNECTION_ALREADY_OPEN,
	                                           FILE_STATUS_OKAY,
	                                           RESTART_MARKER_REPLY,
	                                           CLOSING_DATA_CONNECTION,
	                                           REQUESTED_FILE_ACTION_OKAY,
	                                           CANT_OPEN_DATA_CONNECTION,
	                                           CONNECTION_CLOSED_TRANSFER_ABORTED,
	                                           REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING,
	                                           REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
	                                           REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE,
	                                           NEED_ACCOUNT_FOR_STORING_FILES,
	                                           REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                                           REQUESTED_ACTION_NOT_TAKEN_INSUFFICIENT_STORAGE_SPACE,
	                                           REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
	                                           SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                           SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                           SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                           NOT_LOGGED_IN))),
	/**
	 * 检索
	 *
	 * <p>此命令使服务器-DTP将指定路径名中的文件副本传输到数据连接另一端的服务器或用户-DTP。服务器站点上的文件的状态和内容不受影响。</p>
	 */
	RETRIEVE("RETR", new HashSet<>(List.of(DATA_CONNECTION_ALREADY_OPEN,
	                                       FILE_STATUS_OKAY,
	                                       RESTART_MARKER_REPLY,
	                                       CLOSING_DATA_CONNECTION,
	                                       REQUESTED_FILE_ACTION_OKAY,
	                                       CANT_OPEN_DATA_CONNECTION,
	                                       CONNECTION_CLOSED_TRANSFER_ABORTED,
	                                       REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING,
	                                       REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                                       REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS,
	                                       SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                       SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                       SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                       NOT_LOGGED_IN))),
	/**
	 * 列表
	 *
	 * <p>此命令导致服务器将列表从服务器发送到被动DTP。如果路径名指定了一个目录或其他文件组，则服务器应传输指定目录中的文件列表。
	 * 如果路径名指定了一个文件，则服务器应发送有关该文件的当前信息。空参数意味着用户的当前工作或默认目录。 数据传输在类型ASCII或类型EBCDIC上通过数据连接进行。（用户必须确保类型适当地为ASCII或EBCDIC）。
	 * 由于不同系统上关于文件的信息可能差异很大，这些信息可能很难在程序中自动使用，但对于人类用户可能非常有用。</p>
	 */
	LIST("LIST", new HashSet<>(List.of(DATA_CONNECTION_ALREADY_OPEN,
	                                   FILE_STATUS_OKAY,
	                                   CLOSING_DATA_CONNECTION,
	                                   REQUESTED_FILE_ACTION_OKAY,
	                                   CANT_OPEN_DATA_CONNECTION,
	                                   CONNECTION_CLOSED_TRANSFER_ABORTED,
	                                   REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING,
	                                   REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                                   SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                   SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                   COMMAND_NOT_IMPLEMENTED,
	                                   SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                   NOT_LOGGED_IN))),
	/**
	 * 名称列表
	 *
	 * <p>此命令导致目录列表从服务器发送到用户站点。路径名应指定一个目录或其他系统特定的文件组描述符；空参数意味着当前目录。
	 * 服务器将返回文件名流，而没有其他信息。数据将以ASCII或EBCDIC类型通过数据连接传输，作为有效的路径名字符串，由或分隔。（
	 * 同样，用户必须确保类型正确。）此命令旨在返回可以由程序进一步自动处理文件的信息。例如，在实现“多个获取”功能时。</p>
	 */
	NAME_LIST("NLST", new HashSet<>(List.of(DATA_CONNECTION_ALREADY_OPEN,
	                                        FILE_STATUS_OKAY,
	                                        CLOSING_DATA_CONNECTION,
	                                        REQUESTED_FILE_ACTION_OKAY,
	                                        CANT_OPEN_DATA_CONNECTION,
	                                        CONNECTION_CLOSED_TRANSFER_ABORTED,
	                                        REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING,
	                                        REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                                        SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                        SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                        COMMAND_NOT_IMPLEMENTED,
	                                        SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                        NOT_LOGGED_IN))),
	/**
	 * 追加（带创建）
	 *
	 * <p>此命令使服务器-DTP接受通过数据连接传输的数据，并将数据存储在服务器站点的文件中。
	 * 如果路径名中指定的文件在服务器站点存在，则数据将被追加到该文件中；否则，将在服务器站点创建路径名中指定的文件。</p>
	 */
	APPEND("APPE",
	       new HashSet<>(List.of(DATA_CONNECTION_ALREADY_OPEN,
	                             FILE_STATUS_OKAY,
	                             RESTART_MARKER_REPLY,
	                             CLOSING_DATA_CONNECTION,
	                             REQUESTED_FILE_ACTION_OKAY,
	                             CANT_OPEN_DATA_CONNECTION,
	                             CONNECTION_CLOSED_TRANSFER_ABORTED,
	                             REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING,
	                             REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
	                             REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE,
	                             NEED_ACCOUNT_FOR_STORING_FILES,
	                             REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                             REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS,
	                             REQUESTED_ACTION_NOT_TAKEN_INSUFFICIENT_STORAGE_SPACE,
	                             REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
	                             SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                             SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                             COMMAND_NOT_IMPLEMENTED,
	                             SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                             NOT_LOGGED_IN))),
	/**
	 * 重命名从
	 *
	 * <p>此命令指定要重命名的文件的旧路径名。此命令必须立即由指定新文件路径名的“重命名到”命令跟随。两个命令一起导致文件被重命名。</p>
	 */
	RENAME_FROM("RNFR",
	            new HashSet<>(List.of(REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                                  REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS,
	                                  SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                  SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                  COMMAND_NOT_IMPLEMENTED,
	                                  SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                  NOT_LOGGED_IN,
	                                  REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION
	                                 ))),
	/**
	 * 重命名到
	 *
	 * <p>此命令指定在紧接前面的“重命名从”命令中指定的文件的新路径名。两个命令一起导致一个文件被重命名。</p>
	 */
	RENAME_TO("RNTO", new HashSet<>(List.of(REQUESTED_FILE_ACTION_OKAY,
	                                        NEED_ACCOUNT_FOR_STORING_FILES,
	                                        REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
	                                        SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                        SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                        COMMAND_NOT_IMPLEMENTED,
	                                        BAD_SEQUENCE_OF_COMMANDS,
	                                        SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                        NOT_LOGGED_IN))),
	/**
	 * 删除
	 *
	 * <p>此命令导致在路径名中指定的文件在服务器站点被删除。
	 * 如果需要额外的保护级别（如查询，“您真的希望删除吗？”），则应由用户-FTP进程提供。</p>
	 */
	DELETE("DELE", new HashSet<>(List.of(REQUESTED_FILE_ACTION_OKAY,
	                                     REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                                     REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS,
	                                     SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                     SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                     COMMAND_NOT_IMPLEMENTED,
	                                     SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                     NOT_LOGGED_IN))),
	/**
	 * 删除目录
	 *
	 * <p>此命令导致在路径名中指定的目录被删除，作为一个目录（如果路径名是绝对的）或作为当前工作目录的子目录（如果路径名是相对的）。</p>
	 */
	REMOVE_DIRECTORY("RMD", new HashSet<>(List.of(REQUESTED_FILE_ACTION_OKAY,
	                                              SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                              SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                              COMMAND_NOT_IMPLEMENTED,
	                                              SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                              NOT_LOGGED_IN,
	                                              REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS))),
	/**
	 * 创建目录
	 *
	 * <p>此命令导致在路径名中指定的目录被创建为一个目录（如果路径名是绝对的）或作为当前工作目录的子目录（如果路径名是相对的）。</p>
	 */
	MAKE_DIRECTORY("MKD", new HashSet<>(List.of(PATHNAME_CREATED,
	                                            SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                            SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                            COMMAND_NOT_IMPLEMENTED,
	                                            SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                            NOT_LOGGED_IN,
	                                            REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS))),
	/**
	 * 打印工作目录
	 *
	 * <p>此命令导致在回复中返回当前工作目录的名称。</p>
	 */
	PRINT_WORKING_DIRECTORY("PWD", new HashSet<>(List.of(PATHNAME_CREATED,
	                                                     SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                                     SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                                     COMMAND_NOT_IMPLEMENTED,
	                                                     SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                                                     REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS))),
	/**
	 * 中止
	 *
	 * <p>此命令告诉服务器中止前一个FTP服务命令及其相关的数据传输。中止命令可能需要“特殊操作”，如FTP命令部分所讨论的，以强制服务器识别。
	 * 如果前一个命令已经完成（包括数据传输），则不采取任何行动。服务器不应关闭控制连接，但必须关闭数据连接。 服务器收到此命令有两种情况：1. FTP服务命令已经完成 2. FTP服务命令仍在进行中。
	 * 在第一种情况下，如果数据连接已经打开，服务器关闭数据连接，并以226回复响应，表明中止命令已成功处理。 在第二种情况下，服务器中止正在进行的FTP服务并关闭数据连接，返回426回复以指示服务请求异常终止。
	 * 然后服务器发送226回复，表明中止命令已成功处理。</p>
	 */
	ABORT("ABOR",
	      new HashSet<>(List.of(DATA_CONNECTION_OPEN_NO_TRANSFER_IN_PROGRESS,
	                            CLOSING_DATA_CONNECTION,
	                            SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                            SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                            COMMAND_NOT_IMPLEMENTED,
	                            SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 系统
	 *
	 * <p>此命令用于找出服务器的操作系统类型。回复的第一个词应该是在当前版本的Assigned Numbers文档[4]中列出的系统名称之一。</p>
	 */
	SYSTEM("SYST", new HashSet<>(List.of(NAME_SYSTEM_TYPE, SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                     SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                     COMMAND_NOT_IMPLEMENTED, SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 状态
	 *
	 * <p>此命令将导致通过控制连接以回复形式发送状态响应。该命令可以在文件传输期间发送（连同Telnet IP和同步信号——见FTP命令部分），
	 * 在这种情况下，服务器将响应正在进行的操作的状态，或者可以在文件传输之间发送。在后一种情况下，命令可能有一个参数字段。 如果参数是路径名，则该命令类似于“list”命令，只是数据将通过控制连接传输。
	 * 如果给出部分路径名，服务器可能会回复与该规范相关的文件名或属性列表。 如果没有给出参数，则服务器应返回有关服务器FTP进程的一般状态信息。这应该包括所有传输参数的当前值和连接的状态。</p>
	 */
	STATUS("STAT",
	       new HashSet<>(List.of(SYSTEM_STATUS_OR_HELP_REPLY,
	                             DIRECTORY_STATUS,
	                             FILE_STATUS,
	                             REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE,
	                             SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                             SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                             COMMAND_NOT_IMPLEMENTED,
	                             SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
	                             NOT_LOGGED_IN))),
	/**
	 * 帮助
	 *
	 * <p>此命令将导致服务器通过控制连接向用户发送有关其实现状态的有用信息。该命令可以带有一个参数（例如，任何命令名称），
	 * 并返回更具体的信息作为响应。回复类型为211或214。建议在输入USER命令之前允许使用HELP。 服务器可以使用此回复来指定站点依赖的参数，例如，响应于HELP SITE。</p>
	 */
	HELP("HELP", new HashSet<>(List.of(SYSTEM_STATUS_OR_HELP_REPLY, HELP_MESSAGE, SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                   SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                                   COMMAND_NOT_IMPLEMENTED, SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION))),
	/**
	 * 站点参数
	 *
	 * <p>此命令由服务器用来提供对其系统特定的、对文件传输至关重要但不足以作为协议中的命令包括的服务。
	 * 这些服务的性质和语法规范可以在对HELP SITE命令的回复中说明。</p>
	 */
	SITE("SITE",
	     new HashSet<>(List.of(COMMAND_OKAY,
	                           COMMAND_NOT_IMPLEMENTED_SUPERFLUOUS,
	                           SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                           SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
	                           NOT_LOGGED_IN))),
	/**
	 * 无操作
	 *
	 * <p>此命令不影响任何参数或之前输入的命令。它指定的唯一操作是服务器发送OK回复。</p>
	 */
	NOOP("NOOP",
	     new HashSet<>(Arrays.asList(COMMAND_OKAY,
	                                 SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
	                                 SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION)));

	private final String         command;
	private final Set<ReplyCode> validReplyCodes;

	Command(String command, Set<ReplyCode> validCodes) {
		this.command         = command;
		this.validReplyCodes = validCodes;
	}

	public String getCommand() {
		return command;
	}

	public Boolean isValidReplyCode(ReplyCode replyCode) {
		return validReplyCodes.contains(replyCode);
	}
}
