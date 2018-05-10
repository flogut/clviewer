;This file will be executed next to the application bundle image
;I.e. current directory will contain folder clviewer with application files
[Setup]
AppId={{de.hgv.app}}
AppName=clviewer
AppVersion=1.0
AppVerName=clviewer 1.0
AppPublisher=Humboldt-Gymnasium Vaterstetten
AppComments=clviewer
AppCopyright=Copyright (C) 2018
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={pf}\clviewer
DisableStartupPrompt=Yes
DisableDirPage=Auto
DisableProgramGroupPage=Auto
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=Yes
DefaultGroupName=Humboldt-Gymnasium Vaterstetten
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=clviewer-1.0
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile=clviewer\clviewer.ico
UninstallDisplayIcon={app}\clviewer.ico
UninstallDisplayName=clviewer
WizardImageStretch=No
WizardSmallImageFile=clviewer-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64

[Tasks]
Name: desktopicon; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";
Name: desktopicon\common; Description: "{cm:ForAllUsers}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: exclusive 
Name: desktopicon\user; Description: "{cm:CurrentUserOnly}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: exclusive unchecked
Name: quicklaunchicon; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}";

[Languages]
Name: "en"; MessagesFile: "compiler:Default.isl"
Name: "de"; MessagesFile: "compiler:languages\German.isl"

[CustomMessages]
en.ForAllUsers=For all users
de.ForAllUsers=Für alle Benutzer
en.CurrentUserOnly=Current user only
de.CurrentUserOnly=Nur für den aktuellen Benutzer

[Files]
Source: "clviewer\clviewer.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "clviewer\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\clviewer"; Filename: "{app}\clviewer.exe"; IconFilename: "{app}\clviewer.ico"; Tasks: quicklaunchicon
Name: "{commondesktop}\clviewer"; Filename: "{app}\clviewer.exe";  IconFilename: "{app}\clviewer.ico"; Tasks: desktopicon\common
Name: "{userdesktop}\clviewer"; Filename:"{app}\clviewer.exe"; IconFilename: "{app}\clviewer.ico"; Tasks: desktopicon\user


[Run]
Filename: "{app}\clviewer.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\clviewer.exe"; Description: "{cm:LaunchProgram,clviewer}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\clviewer.exe"; Parameters: "-install -svcName ""clviewer"" -svcDesc ""clviewer"" -mainExe ""clviewer.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\clviewer.exe "; Parameters: "-uninstall -svcName clviewer -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
