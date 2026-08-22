import os
import re

def replace_in_file(filepath, pattern, replacement, flags=0):
    if not os.path.exists(filepath):
        return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    new_content = re.sub(pattern, replacement, content, flags=flags)
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

def main():
    # 1. Update build files
    for build_file in ['androidApp/build.gradle.kts', 'app/build.gradle.kts']:
        replace_in_file(build_file, r'applicationId\s*=\s*"echo\.music\.iad1tya"', 'applicationId = "com.music.lyra"')
    for gs_file in ['androidApp/google-services.json', 'app/google-services.json']:
        replace_in_file(gs_file, r'echo\.music\.iad1tya', 'com.music.lyra')

    # 2. Update all XML and KT files dynamically
    for root, _, files in os.walk('.'):
        for file in files:
            if file.endswith('.xml') or file.endswith('.kt'):
                filepath = os.path.join(root, file)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    new_content = content.replace('>EchoMusic<', '>LYRA<').replace('>Echo Music<', '>LYRA<')
                    new_content = new_content.replace('>EchoMusic Dev<', '>LYRA Dev<').replace('In EchoMusic,', 'In LYRA,')
                    new_content = new_content.replace('"EchoMusic"', '"LYRA"')
                    # Reroute updater to LYRA repo
                    new_content = new_content.replace('EchoMusicApp/Echo-Music', 'freeforwebsite/LYRA')
                    new_content = new_content.replace('echomusic.apk', 'lyra.apk')

                    if content != new_content:
                        with open(filepath, 'w', encoding='utf-8') as f:
                            f.write(new_content)
                except Exception as e:
                    pass

    # 3. Specific UI removals
    main_activity = 'androidApp/src/main/java/echo/music/iad1tya/MainActivity.kt'
    replace_in_file(main_activity, r'if\s*\(!EasyPermissions\.hasPermissions\(this,\s*Manifest\.permission\.POST_NOTIFICATIONS\)\)\s*\{.*?(?=viewModel\.getLocation\(\))', '', flags=re.DOTALL)
    
    welcome_dialog = 'app/src/main/kotlin/com/music/echo/ui/screens/WelcomeDialog.kt'
    replace_in_file(welcome_dialog, r'WelcomeSectionCard\(title\s*=\s*"Support(?: Echo)?"\).*?(?=Spacer\(modifier = Modifier\.height\(4\.dp\)\))', '', flags=re.DOTALL)
    replace_in_file(welcome_dialog, r'WelcomeSectionCard\(title\s*=\s*"Social Community"\).*?(?=Spacer\(modifier = Modifier\.height\(4\.dp\)\))', '', flags=re.DOTALL)
    replace_in_file(welcome_dialog, r'Button\(\s*onClick\s*=\s*\{\s*uriHandler\.openUri\("https://github\.com/freeforwebsite/LYRA"\)\s*\}.*?(?=Button\(\s*onClick\s*=\s*onDismissRequest)', '', flags=re.DOTALL)
    
    support_dialog = 'composeApp/src/commonMain/kotlin/echo/music/iad1tya/ui/component/SupportProjectDialog.kt'
    replace_in_file(support_dialog, r'Text\(\s*text\s*=\s*"If you enjoy LYRA.*?(?=}\s*},\s*confirmButton\s*=)', 'Text(text = "If you enjoy LYRA, thank you!", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))\n            ', flags=re.DOTALL)
    
    about_screen = 'app/src/main/kotlin/com/music/echo/ui/screens/settings/AboutScreen.kt'
    replace_in_file(about_screen, r'item\s*\{\s*AboutSectionCard\(title\s*=\s*"Developer"\).*?\}\s*\}\s*item\s*\{\s*AboutSectionCard\(title\s*=\s*"Support"\).*?\}\s*\}', '', flags=re.DOTALL)

if __name__ == "__main__":
    main()
