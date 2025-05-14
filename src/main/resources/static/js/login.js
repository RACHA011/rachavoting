/**
 * Auth Form Management
 * Handles tab switching, password validation, and form submission
 */
class AuthForm {
    constructor() {
      this.loginTab = document.getElementById('login-tab');
      this.registerTab = document.getElementById('register-tab');
      this.loginContent = document.getElementById('login-content');
      this.registerContent = document.getElementById('register-content');
      this.registerButton = document.getElementById('register');
      this.registerPassword = document.getElementById('register-password');
      this.confirmPassword = document.getElementById('confirm-password');
      
      this.init();
    }
  
    init() {
      this.setupTabEvents();
      this.setupPasswordToggles();
      this.setupPasswordValidation();
      this.checkUrlParams();
    }
  
    setupTabEvents() {
      this.loginTab.addEventListener('click', () => this.switchTab('login'));
      this.registerTab.addEventListener('click', () => this.switchTab('register'));
    }
  
    switchTab(activeTab) {
      const isLogin = activeTab === 'login';
      
      // Update tab styles
      this.loginTab.classList.toggle('bg-white', isLogin);
      this.loginTab.classList.toggle('text-gray-500', !isLogin);
      this.loginTab.classList.toggle('shadow-sm', isLogin);
      
      this.registerTab.classList.toggle('bg-white', !isLogin);
      this.registerTab.classList.toggle('text-gray-500', isLogin);
      this.registerTab.classList.toggle('shadow-sm', !isLogin);
      
      // Update content visibility
      this.loginContent.classList.toggle('hidden', !isLogin);
      this.registerContent.classList.toggle('hidden', isLogin);
      
      // Update URL
      this.updateUrlParam(activeTab);
    }
  
    updateUrlParam(tab) {
      const url = new URL(window.location);
      url.searchParams.set('tab', tab);
      window.history.pushState({}, '', url);
    }
  
    setupPasswordToggles() {
      document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', () => this.togglePasswordVisibility(button));
      });
    }
  
    togglePasswordVisibility(button) {
      const targetId = button.getAttribute('data-target');
      const input = document.getElementById(targetId);
      const icon = button.querySelector('svg');
      
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      
      icon.innerHTML = isPassword ? 
        `<path d="m15 18-.722-3.25"/>
         <path d="M2 8a10.645 10.645 0 0 0 20 0"/>
         <path d="m20 15-1.726-2.05"/>
         <path d="m4 15 1.726-2.05"/>
         <path d="m9 18 .722-3.25"/>` :
        `<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/>
         <circle cx="12" cy="12" r="3"/>`;
    }
  
    setupPasswordValidation() {
      // Validate on input for both password fields
      [this.registerPassword, this.confirmPassword].forEach(input => {
        input.addEventListener('input', () => this.validatePasswords());
      });
      
      // Initial validation
      this.validatePasswords();
    }
  
    validatePasswords() {
      const password = this.registerPassword.value;
      const confirmPassword = this.confirmPassword.value;
      
      // Check if passwords meet requirements
      const isValid = password.length >= 6 && 
                      confirmPassword.length >= 6 && 
                      password === confirmPassword;
      
      // Enable/disable register button
      this.registerButton.disabled = !isValid;
      
      // Visual feedback
      this.registerButton.classList.toggle('bg-green-600', isValid);
      this.registerButton.classList.toggle('bg-gray-400', !isValid);
      this.registerButton.classList.toggle('hover:bg-green-700', isValid);
      this.registerButton.classList.toggle('cursor-not-allowed', !isValid);
    }
  
    checkUrlParams() {
      const urlParams = new URLSearchParams(window.location.search);
      const tab = urlParams.get('tab');
      this.switchTab(tab === 'register' ? 'register' : 'login');
    }
  }
  
  // Initialize when DOM is loaded
  document.addEventListener('DOMContentLoaded', () => {
    new AuthForm();
  });
  