document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('delAllConv').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'flex';
    });

    document.getElementById('confirmDel').addEventListener('click', function() {
        const dialogContainer = document.getElementById('confirmationDialog');

        // Solicitud backend
        fetch('/delAllConvs', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Algo salió mal al eliminar la conversación.');
            }
            return response.json();
        })
        .then(data => {
            window.location.href = '/allConversations';
        })
        .catch(error => {
            console.error('Error:', error);
        });

        document.getElementById('confirmationDialog').style.display = 'none';
    });

    document.getElementById('cancelDel').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'none';
    });

    document.querySelectorAll('.conversation-name').forEach(item => {
        item.addEventListener('click', () => {
            const convID = item.getAttribute('data-convID');
            fetch('/getConvData', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ convID: convID })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Algo salió mal');
                }
                return response.json();
            })
            .then(data => {
                console.log(data)
                if(data.status === 3) {
                    window.location.href = '/conversationLog';
                } else {
                    window.location.href = '/conversation';
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
        });
    });
});
