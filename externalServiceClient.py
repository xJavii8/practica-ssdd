import requests
import sys
import os
import hashlib
from datetime import datetime

EXTERNAL_SERVICE_URL = "http://localhost:8180/ExternalService/"
userID = ""
userEmail = ""
username = ""
userToken = ""
convName = ""
convID = ""
convNextURL = ""
convEndURL = ""
convMessages = None

def main():
    print('\nBienvenido al cliente del servicio REST externo de LlamaChat.')
    while True:
        print('Estado: \033[91mno autenticado\033[0m.')
        print('Opciones disponibles:')
        print('1. Registrarse')
        print('2. Iniciar sesión')
        print('3. Salir')
        choice = input("\nElige una opción: ")
        
        if choice == '1':
            register()
        elif choice == '2':
            login()
        elif choice == '3':
            print("\033[91mSaliendo...\033[0m")
            sys.exit()
        else:
            print("Opción \033[91mno válida\033[0m. Inténtalo de nuevo.\n")
        
        if userID != "":
            break
        
    clearScreen()
    print("\033[92mHas iniciado sesión correctamente. Bienvenido\033[0m")

    while True:
        print("Estado: \033[92mautenticado\033[0m.")
        print("Opciones disponibles:")
        print("1. Crear una conversación")
        print("2. Ver la lista de conversaciones")
        print("3. Ver mis datos y estadísticas")
        print("4. Eliminar mi usuario")
        print("5. Salir")

        choice = input("\nElige una opción: ")

        if choice == "1":
            createConv()
        elif choice == "2":
            checkConvList()
        elif choice == "3":
            checkUserData()
        elif choice == "4":
            deleteUser()
        elif choice == '5':
            print("\033[91mSaliendo...\033[0m")
            sys.exit()
        else:
            print("Opción \033[91mno válida\033[0m. Inténtalo de nuevo.\n")


def register():
    global userID
    global username
    global userEmail
    global userToken
    print("\nOpción elegida: \033[92mregistro\033[0m.")
    email = input("Correo electrónico: ")
    name = input("Nombre de usuario: ")
    password = input("Contraseña: ")
    json = {"email": email, "name": name, "password": password}
    response = requests.post(EXTERNAL_SERVICE_URL + "u/register", json=json)
    if response.status_code == 200:
        responseJSON = response.json()
        userID = responseJSON['id']
        username = responseJSON['name']
        userEmail = responseJSON['email']
        userToken = responseJSON['token']
    else:
        print(f"\033[91mHa ocurrido un error al hacer el registro. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")

def login():
    global userID
    global username
    global userEmail
    global userToken
    print("\nOpción elegida: \033[92miniciar sesión\033[0m.")
    email = input("Correo electrónico: ")
    password = input("Contraseña: ")
    json = {"email": email, "password": password}
    response = requests.post(EXTERNAL_SERVICE_URL + "checkLogin", json=json)
    if response.status_code == 200:
        responseJSON = response.json()
        userID = responseJSON['id']
        username = responseJSON['name']
        userEmail = responseJSON['email']
        userToken = responseJSON['token']
    else:
        print(f"\033[91mHa ocurrido un error al iniciar sesión. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")

def createConv():
    global convName
    global convID
    global convNextURL
    global convEndURL
    global convMessages
    print("\nOpción elegida: \033[92mcrear una conversación\033[0m.")
    convName = input("Nombre de la conversación: ")

    url = EXTERNAL_SERVICE_URL + f"u/{userID}/dialogue"
    currentDate = datetime.now().strftime("%Y-%m-%d")
    authToken = genAuthToken(url, currentDate, userToken)

    headers = {
        "User": userID,
        "Date": currentDate,
        "Auth-Token": authToken
    }

    requestJSON = {"convName": convName}
    response = requests.post(url, json=requestJSON, headers=headers)

    if response.status_code == 201:
        responseJSON = response.json()
        convName = responseJSON.get('name')
        convID = responseJSON.get('ID')
        convNextURL = responseJSON.get('nextURL')
        convEndURL = responseJSON.get('endURL')
        convMessages = None
        print("\033[92mConversación creada correctamente.\033[0m")
        sendPrompts()
    else:
        print(f"\033[91mHa ocurrido un error al crear la conversación. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")

def checkConvList():
    print("\nOpción elegida: \033[92mver la lista de conversaciones\033[0m.")
    url = EXTERNAL_SERVICE_URL + f"u/{userID}/dialogue"
    currentDate = datetime.now().strftime("%Y-%m-%d")
    authToken = genAuthToken(url, currentDate, userToken)

    headers = {
        "User": userID,
        "Date": currentDate,
        "Auth-Token": authToken
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        allConvs = response.json().get('allConvs', [])
        if allConvs:
            print("Conversaciones disponibles: ")
            convStatus = {1: '\033[92mabierta\033[0m', 2: '\033[93mocupada\033[0m', 3: '\033[91mcerrada\033[0m'}
            for index, conv in enumerate(allConvs, 1):
                name = conv.get('name')
                status = conv.get('status')
                statusDescription = convStatus.get(status, '\033[90mdesconocido\033[0m')
                print(f"{index}. {name} - Estado: {statusDescription}")
            print("\nOpciones disponibles:")
            print("1. Entrar a una conversación")
            print("2. Eliminar todas las conversaciones")
            print("3. Volver al menú principal")
            choice = input("\nElige una opción: ")

            if choice == "1":
                conv_choice = int(input("Elige el número de la conversación a la que deseas entrar: "))
                if 1 <= conv_choice <= len(allConvs):
                    enterConversation(allConvs[conv_choice - 1]['id'])
                else:
                    print("\033[91mNúmero de conversación no válido. Volviendo al menú principal...\033[0m\n")
            elif choice == "2":
                print("\nOpción elegida: \033[92meliminar todas las conversaciones\033[0m.")
                print("¿Estás seguro de que quieres eliminar todas las conversaciones? \033[91mEsta acción es irreversible.\033[0m")
                print("1. Sí")
                print("2. No")
                choiceDelAllConvs = input("\nOpción elegida: ")

                if choiceDelAllConvs == "1":
                    print("\033[91mEliminando todas las conversaciones...\033[0m")
                    
                    url = EXTERNAL_SERVICE_URL + f"u/{userID}/delAllConvs"
                    currentDate = datetime.now().strftime("%Y-%m-%d")
                    authToken = genAuthToken(url, currentDate, userToken)

                    headers = {
                        "User": userID,
                        "Date": currentDate,
                        "Auth-Token": authToken
                    }

                    response = requests.delete(url, headers=headers)

                    if response.status_code == 200:
                        print("\033[92mSe han eliminado todas las conversaciones.\033[0m\n")
                        return
                    else:
                        print(f"\033[91mHa ocurrido un error al eliminar todas las conversaciones. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")
                elif choiceDelAllConvs == "2":
                    print("\033[92mVolviendo al menú principal...\033[0m\n")
                    return
                else:
                    print("Opción \033[91mno válida\033[0m. Volverás al menú principal\n")
                    return

            elif choice == "3":
                print("\033[92mVolviendo al menú principal...\033[0m\n")
                return
            else:
                print("Opción \033[91mno válida\033[0m. Se volverá al menú principal.\n")
        else:
            print("\033[91mNo hay conversaciones disponibles.\033[0m\n")
    else:
        print(f"\033[91mHa ocurrido un error al obtener todas las conversaciones. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")

def enterConversation(enterConvID):
    global convID
    global convName
    global convNextURL
    global convEndURL
    global convMessages

    url = EXTERNAL_SERVICE_URL + f"u/{userID}/dialogue/{enterConvID}"
    currentDate = datetime.now().strftime("%Y-%m-%d")
    authToken = genAuthToken(url, currentDate, userToken)
    headers = {
        "User": userID,
        "Date": currentDate,
        "Auth-Token": authToken
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        responseJSON = response.json()
        convID = responseJSON.get('convID')
        convName = responseJSON.get('convName')
        convNextURL = responseJSON.get('nextURL')
        convEndURL = responseJSON.get('endURL')
        convMessages = responseJSON.get('dialogues')
        convStatus = responseJSON.get('status')
        if convStatus == 3:
            conversationLog()
        else:
            sendPrompts()
    else:
        print(f"\033[91mHa ocurrido un error al obtener los datos de la conversación. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")

def conversationLog():
    global convName
    global convID
    global convMessages
    print(f"\n\033[92mConversación actual: \033[35m{convName}\033[0m")
    if convMessages is not None:
        for dialogue in convMessages:
            print("\033[92mPregunta: \033[35m" + dialogue['prompt'])
            print("\033[92mRespuesta: \033[35m" + dialogue['answer'] + "\n")
    
    print("\n\033[0mOpciones disponibles:")
    print("1. Eliminar la conversación")
    print("2. Volver al menú principal")
    choice = input("\nElige una opción: ")

    if choice == "1":
        print("\nOpción elegida: \033[92meliminar la conversación\033[0m.")
        print("¿Estás seguro de que quieres eliminar la conversación? \033[91mEsta acción es irreversible.\033[0m")
        print("1. Sí")
        print("2. No")
        choiceDelConv = input("\nOpción elegida: ")

        if choiceDelConv == "1":
            print("\033[91mEliminando la conversación...\033[0m")
            
            url = EXTERNAL_SERVICE_URL + f"u/{userID}/dialogue/{convID}/del"
            currentDate = datetime.now().strftime("%Y-%m-%d")
            authToken = genAuthToken(url, currentDate, userToken)

            headers = {
                "User": userID,
                "Date": currentDate,
                "Auth-Token": authToken
            }

            response = requests.delete(url, headers=headers)

            if response.status_code == 200:
                print("\033[92mSe ha eliminado la conversación.\033[0m\n")
                return
            else:
                print(f"\033[91mHa ocurrido un error al eliminar la conversación. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")
        elif choiceDelConv == "2":
            print("\033[92mVolviendo al menú principal...\033[0m\n")
            return
        else:
            print("Opción \033[91mno válida\033[0m. Volverás al menú principal\n")
            return
    elif choice == "2":
        print("\033[92mVolviendo al menú principal...\033[0m\n")
        return
    else:
        print("Opción \033[91mno válida\033[0m. Volverás al menú principal\n")
        return


def sendPrompts():
    global convName
    global convID
    global convNextURL
    global convEndURL
    global convMessages
    print(f"\n\033[92mConversación actual: \033[35m{convName}\033[0m")

    if convMessages is not None:
        for dialogue in convMessages:
            print("\033[92mPregunta: \033[35m" + dialogue['prompt'])
            print("\033[92mRespuesta: \033[35m" + dialogue['answer'] + "\n")

    while True:
        print("\033[92mLeyenda:\n- \033[35m\"q\"\033[92m: salir de la conversación\n- \033[35m\"end\"\033[92m: terminar la conversación\033[0m\n")
        prompt = input("Introduce el prompt a enviar: ")
        print(f"\033[92mPrompt detectado: \033[35m{prompt}\033[0m")
        if prompt == "end":
            print("\033[91mTerminando la conversación...\033[0m")
            url = f"http://localhost:8180/ExternalService{convEndURL}"
            currentDate = datetime.now().strftime("%Y-%m-%d")
            authToken = genAuthToken(url, currentDate, userToken)
            headers = {
                "User": userID,
                "Date": currentDate,
                "Auth-Token": authToken
            }

            response = requests.post(url, headers=headers)
            if response.status_code == 200:
                convID = ""
                convName = ""
                convNextURL = ""
                convEndURL = ""
                convMessages = None
                print("\033[92mSe ha finalizado correctamente la conversación.\033[0m\n")
                break
            else:
                print(f"\033[91mHa ocurrido un error al finalizar la conversación. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")
        elif prompt == "q":
            print("\033[92mVolviendo al menú principal...\033[0m\n")
            return
        else:
            print("\033[92mEnviando prompt...\033[0m")
            timestamp = datetime.now()
            timestamp = datetime.timestamp(timestamp)
            timestamp = round(timestamp * 1000)
            requestJSON = {'userID': userID, 'convID': convID, 'prompt': prompt, 'timestamp': timestamp}

            url = f"http://localhost:8180/ExternalService{convNextURL}"
            currentDate = datetime.now().strftime("%Y-%m-%d")
            authToken = genAuthToken(url, currentDate, userToken)
            headers = {
                "User": userID,
                "Date": currentDate,
                "Auth-Token": authToken
            }

            response = requests.post(url, json=requestJSON, headers=headers)

            if response.status_code == 200:
                answerJSON = response.json()
                lastAnswer = answerJSON['dialogues'][-1]['answer']
                print(f"\033[92mRespuesta: \033[35m{lastAnswer}\033[0m\n")
            elif response.status_code == 204:
                print(f"\033[91mError: esta conversación aún no está lista para mandar más mensajes\033[0m\n")
            else:
                print(f"\033[91mHa ocurrido un error al enviar el prompt. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")

def checkUserData():
    print("\nOpción elegida: \033[92mver mis datos y estadísticas\033[0m.")

    url = EXTERNAL_SERVICE_URL + f"u/{userID}/stats"
    currentDate = datetime.now().strftime("%Y-%m-%d")
    authToken = genAuthToken(url, currentDate, userToken)

    headers = {
        "User": userID,
        "Date": currentDate,
        "Auth-Token": authToken
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        statsJSON = response.json()
        createdConvs = statsJSON.get('createdConvs', 0)
        promptCalls = statsJSON.get('promptCalls', 0)
    else:
        print(f"\033[91mHa ocurrido un error al obtener tus estadísticas. Código: {response.status_code}\033[0m")
        createdConvs = 0
        promptCalls = 0

    print(f"Email actual: {userEmail}")
    print(f"Nombre de usuario: {username}")
    print(f"Conversaciones creadas: {createdConvs}")
    print(f"Llamadas al prompt: {promptCalls}\n")

def deleteUser():
    print("\nOpción elegida: \033[92meliminar mi usuario\033[0m.")
    print("¿Estás seguro de que quieres eliminar tu usuario? \033[91mEsta acción es irreversible.\033[0m")
    print("1. Sí")
    print("2. No")
    choice = input("\nOpción elegida: ")

    if choice == "1":
        print("\033[91mEliminando usuario...\033[0m")
        
        url = EXTERNAL_SERVICE_URL + f"u/deleteUser/{userID}"
        currentDate = datetime.now().strftime("%Y-%m-%d")
        authToken = genAuthToken(url, currentDate, userToken)

        headers = {
            "User": userID,
            "Date": currentDate,
            "Auth-Token": authToken
        }

        response = requests.delete(url, headers=headers)
        if response.status_code == 200:
            print("Usuario \033[92meliminado\033[0m. Gracias por usar este servicio.")
            print("\033[91mSaliendo...\033[0m")
            sys.exit()
        else:
            print(f"\033[91mHa ocurrido un error al eliminar el usuario. Código: {response.status_code}. Por favor, inténtalo de nuevo\033[0m\n")
    elif choice == "2":
        return
    else:
        print("Opción \033[91mno válida\033[0m. Volverás al menú principal\n")
        return
    
def genAuthToken(url, date, userToken):
    decodedStr = f"{url}{date}{userToken}"
    md5String = hashlib.md5(decodedStr.encode())
    return md5String.hexdigest()

def clearScreen():
    if os.name == 'nt':
        os.system('cls')
    else:
        os.system('clear')

if __name__ == "__main__":
    main()
