document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the account details page
    const depositBtn = document.getElementById('depositSubmitBtn');
    const withdrawBtn = document.getElementById('withdrawSubmitBtn');
    
    if (depositBtn || withdrawBtn) {
        // Get account details from page data attributes or URL
        const accountNumber = document.querySelector('[data-account-number]')?.dataset.accountNumber || 
                             window.location.pathname.split('/').pop();
        
        console.log('Account operations initialized for account:', accountNumber);
        
        // Deposit functionality
        if (depositBtn) {
            depositBtn.addEventListener('click', function() {
                const depositAmount = document.getElementById('depositAmount').value;
                const depositDescription = document.getElementById('depositDescription').value;
                const depositErrorAlert = document.getElementById('depositErrorAlert');
                const depositSuccessAlert = document.getElementById('depositSuccessAlert');
                
                // Reset alerts
                depositErrorAlert.classList.add('d-none');
                depositSuccessAlert.classList.add('d-none');
                
                // Validate input
                if (!depositAmount || isNaN(parseFloat(depositAmount)) || parseFloat(depositAmount) <= 0) {
                    depositErrorAlert.textContent = 'Please enter a valid amount greater than zero.';
                    depositErrorAlert.classList.remove('d-none');
                    return;
                }
                
                if (!depositDescription) {
                    depositErrorAlert.textContent = 'Please enter a description.';
                    depositErrorAlert.classList.remove('d-none');
                    return;
                }
                
                // Get authentication token
                const token = localStorage.getItem('token');
                if (!token) {
                    depositErrorAlert.textContent = 'Authentication error. Please log in again.';
                    depositErrorAlert.classList.remove('d-none');
                    return;
                }
                
                // Prepare request data
                const requestData = {
                    amount: parseFloat(depositAmount),
                    description: depositDescription
                };
                
                // Make API call
                fetch(`/api/user/accounts/${accountNumber}/deposit`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(requestData)
                })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(data => {
                            throw new Error(data.message || 'Deposit failed');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Deposit successful:', data);
                    depositSuccessAlert.textContent = `Successfully deposited ₹${parseFloat(depositAmount).toFixed(2)}`;
                    depositSuccessAlert.classList.remove('d-none');
                    
                    // Clear form
                    document.getElementById('depositAmount').value = '';
                    document.getElementById('depositDescription').value = '';
                    
                    // Reload page after 2 seconds
                    setTimeout(() => {
                        window.location.reload();
                    }, 2000);
                })
                .catch(error => {
                    console.error('Deposit error:', error);
                    depositErrorAlert.textContent = error.message || 'An error occurred. Please try again.';
                    depositErrorAlert.classList.remove('d-none');
                });
            });
        }
        
        // Withdraw functionality
        if (withdrawBtn) {
            withdrawBtn.addEventListener('click', function() {
                const withdrawAmount = document.getElementById('withdrawAmount').value;
                const withdrawDescription = document.getElementById('withdrawDescription').value;
                const withdrawErrorAlert = document.getElementById('withdrawErrorAlert');
                const withdrawSuccessAlert = document.getElementById('withdrawSuccessAlert');
                
                // Reset alerts
                withdrawErrorAlert.classList.add('d-none');
                withdrawSuccessAlert.classList.add('d-none');
                
                // Validate input
                if (!withdrawAmount || isNaN(parseFloat(withdrawAmount)) || parseFloat(withdrawAmount) <= 0) {
                    withdrawErrorAlert.textContent = 'Please enter a valid amount greater than zero.';
                    withdrawErrorAlert.classList.remove('d-none');
                    return;
                }
                
                if (!withdrawDescription) {
                    withdrawErrorAlert.textContent = 'Please enter a description.';
                    withdrawErrorAlert.classList.remove('d-none');
                    return;
                }
                
                // Get authentication token
                const token = localStorage.getItem('token');
                if (!token) {
                    withdrawErrorAlert.textContent = 'Authentication error. Please log in again.';
                    withdrawErrorAlert.classList.remove('d-none');
                    return;
                }
                
                // Prepare request data
                const requestData = {
                    amount: parseFloat(withdrawAmount),
                    description: withdrawDescription
                };
                
                // Make API call
                fetch(`/api/user/accounts/${accountNumber}/withdraw`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(requestData)
                })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(data => {
                            throw new Error(data.message || 'Withdrawal failed');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Withdrawal successful:', data);
                    withdrawSuccessAlert.textContent = `Successfully withdrew ₹${parseFloat(withdrawAmount).toFixed(2)}`;
                    withdrawSuccessAlert.classList.remove('d-none');
                    
                    // Clear form
                    document.getElementById('withdrawAmount').value = '';
                    document.getElementById('withdrawDescription').value = '';
                    
                    // Reload page after 2 seconds
                    setTimeout(() => {
                        window.location.reload();
                    }, 2000);
                })
                .catch(error => {
                    console.error('Withdrawal error:', error);
                    withdrawErrorAlert.textContent = error.message || 'An error occurred. Please try again.';
                    withdrawErrorAlert.classList.remove('d-none');
                });
            });
        }
    }
}); 