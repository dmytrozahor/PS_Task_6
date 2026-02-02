#!/bin/bash

################################################################################
# PS_Task_6 Kubernetes Deployment Script
#
# This script deploys the entire PS_Task_6 application stack to GKE
# in the correct order with proper validation at each step.
#
# Usage: ./deploy.sh [options]
#   --full          Full deployment (infrastructure + services)
#   --infra-only    Deploy only infrastructure
#   --services-only Deploy only services (requires infra running)
#   --update        Update existing deployments (rolling update)
#   --check         Check deployment status
#   --cleanup       Delete all resources
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ID="ps-task-6"
ZONE="europe-west1-b"
CLUSTER_NAME="ps-task-cluster"
NAMESPACE="ps-task"
K8S_DIR="k8s"

# Function to print colored messages
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Function to check if kubectl is configured
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl not found. Please install kubectl first."
        exit 1
    fi
    print_success "kubectl found"

    # Check gcloud
    if ! command -v gcloud &> /dev/null; then
        print_error "gcloud not found. Please install Google Cloud SDK first."
        exit 1
    fi
    print_success "gcloud found"

    # Check if K8s directory exists
    if [ ! -d "$K8S_DIR" ]; then
        print_error "K8s directory '$K8S_DIR' not found. Please run from project root."
        exit 1
    fi
    print_success "K8s directory found"

    echo ""
}

# Function to configure kubectl
configure_kubectl() {
    print_header "Configuring kubectl"

    print_info "Getting GKE credentials..."
    gcloud container clusters get-credentials $CLUSTER_NAME \
        --zone=$ZONE \
        --project=$PROJECT_ID

    print_success "Configured kubectl for cluster $CLUSTER_NAME"
    echo ""
}

# Function to create namespace if it doesn't exist
create_namespace() {
    print_header "Setting up Namespace"

    if kubectl get namespace $NAMESPACE &> /dev/null; then
        print_info "Namespace $NAMESPACE already exists"
    else
        print_info "Creating namespace $NAMESPACE..."
        kubectl create namespace $NAMESPACE
        print_success "Namespace created"
    fi

    print_info "Setting $NAMESPACE as default namespace..."
    kubectl config set-context --current --namespace=$NAMESPACE
    print_success "Default namespace set"
    echo ""
}

# Function to check if secrets need updating
check_secrets() {
    print_header "Checking Secrets Configuration"

    local needs_update=false

    # Check IAP secret
    if grep -q "YOUR_OAUTH_CLIENT_ID" $K8S_DIR/secrets/05-iap-secret.yaml; then
        print_warning "IAP secret needs configuration (05-iap-secret.yaml)"
        print_info "  Please update with your OAuth2 credentials from Google Cloud Console"
        needs_update=true
    fi

    # Check Datadog secret
    if grep -q "YOUR_DATADOG_API_KEY" $K8S_DIR/secrets/06-datadog-secret.yaml; then
        print_warning "Datadog secret needs configuration (06-datadog-secret.yaml)"
        print_info "  Please update with your Datadog API key or skip if not using Datadog"
    fi

    if [ "$needs_update" = true ]; then
        echo ""
        read -p "Have you updated the secrets? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Please update secrets before deploying"
            exit 1
        fi
    fi

    print_success "Secrets check complete"
    echo ""
}

# Function to deploy secrets
deploy_secrets() {
    print_header "Deploying Secrets"

    local secrets=(
        "01-postgres-secret.yaml"
        "02-rabbitmq-secret.yaml"
        "03-service-authors-secret.yaml"
        "04-service-email-secret.yaml"
        "05-iap-secret.yaml"
        "06-datadog-secret.yaml"
    )

    for secret in "${secrets[@]}"; do
        print_info "Applying $secret..."
        kubectl apply -f $K8S_DIR/secrets/$secret
    done

    print_success "All secrets deployed"

    # Verify
    print_info "Verifying secrets..."
    kubectl get secrets -n $NAMESPACE
    echo ""
}

# Function to deploy configs
deploy_configs() {
    print_header "Deploying ConfigMaps and BackendConfig"

    print_info "Applying service-authors config..."
    kubectl apply -f $K8S_DIR/config/10-service-authors-config.yaml

    print_info "Applying service-email config..."
    kubectl apply -f $K8S_DIR/config/11-service-email-config.yaml

    print_info "Applying backend config..."
    kubectl apply -f $K8S_DIR/config/12-backendconfig.yaml

    print_success "All configs deployed"

    # Verify
    print_info "Verifying configs..."
    kubectl get configmaps -n $NAMESPACE
    echo ""
}

# Function to deploy certificates
deploy_certificates() {
    print_header "Deploying SSL Certificates"

    print_info "Applying gateway certificate..."
    kubectl apply -f $K8S_DIR/certificates/20-gateway-cert.yaml

    print_info "Applying books UI certificate..."
    kubectl apply -f $K8S_DIR/certificates/21-books-ui-cert.yaml

    print_info "Applying reviews UI certificate..."
    kubectl apply -f $K8S_DIR/certificates/22-reviews-ui-cert.yaml

    print_success "All certificates deployed"

    print_warning "Note: SSL certificates take 10-15 minutes to provision"

    # Verify
    print_info "Current certificate status:"
    kubectl get managedcertificate -n $NAMESPACE
    echo ""
}

# Function to deploy infrastructure
deploy_infrastructure() {
    print_header "Deploying Infrastructure Services"

    # PostgreSQL
    print_info "Deploying PostgreSQL..."
    kubectl apply -f $K8S_DIR/infrastructure/30-postgres.yaml
    print_info "Waiting for PostgreSQL to be ready..."
    kubectl wait --for=condition=ready pod -l app=postgres --timeout=300s -n $NAMESPACE || true
    print_success "PostgreSQL deployed"

    # MongoDB
    print_info "Deploying MongoDB..."
    kubectl apply -f $K8S_DIR/infrastructure/31-mongodb.yaml
    print_info "Waiting for MongoDB to be ready..."
    kubectl wait --for=condition=ready pod -l app=mongodb --timeout=300s -n $NAMESPACE || true
    print_success "MongoDB deployed"

    # RabbitMQ
    print_info "Deploying RabbitMQ..."
    kubectl apply -f $K8S_DIR/infrastructure/32-rabbitmq.yaml
    print_info "Waiting for RabbitMQ to be ready..."
    kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s -n $NAMESPACE || true
    print_success "RabbitMQ deployed"

    # Elasticsearch
    print_info "Deploying Elasticsearch..."
    kubectl apply -f $K8S_DIR/infrastructure/33-elasticsearch.yaml
    print_info "Waiting for Elasticsearch to be ready..."
    kubectl wait --for=condition=ready pod -l app=elasticsearch --timeout=300s -n $NAMESPACE || true
    print_success "Elasticsearch deployed"

    # Mailpit
    print_info "Deploying Mailpit..."
    kubectl apply -f $K8S_DIR/infrastructure/34-mailpit.yaml
    print_info "Waiting for Mailpit to be ready..."
    kubectl wait --for=condition=ready pod -l app=mailpit --timeout=120s -n $NAMESPACE || true
    print_success "Mailpit deployed"

    print_success "All infrastructure services deployed"

    # Show status
    print_info "Infrastructure status:"
    kubectl get pods -n $NAMESPACE -l 'app in (postgres,mongodb,rabbitmq,elasticsearch,mailpit)'
    echo ""
}

# Function to deploy application services
deploy_services() {
    print_header "Deploying Application Services"

    # Gateway
    print_info "Deploying Gateway..."
    kubectl apply -f $K8S_DIR/services/40-gateway.yaml
    print_info "Waiting for Gateway to be ready..."
    kubectl wait --for=condition=ready pod -l app=gateway --timeout=300s -n $NAMESPACE || true
    print_success "Gateway deployed"

    # Service Authors
    print_info "Deploying Service Authors..."
    kubectl apply -f $K8S_DIR/services/41-service-authors.yaml
    print_info "Waiting for Service Authors to be ready..."
    kubectl wait --for=condition=ready pod -l app=service-authors --timeout=300s -n $NAMESPACE || true
    print_success "Service Authors deployed"

    # Service Email
    print_info "Deploying Service Email..."
    kubectl apply -f $K8S_DIR/services/42-service-email.yaml
    print_info "Waiting for Service Email to be ready..."
    kubectl wait --for=condition=ready pod -l app=service-email --timeout=300s -n $NAMESPACE || true
    print_success "Service Email deployed"

    # Service Books
    print_info "Deploying Service Books (UI)..."
    kubectl apply -f $K8S_DIR/services/43-service-book.yaml
    print_info "Waiting for Service Books to be ready..."
    kubectl wait --for=condition=ready pod -l app=service-books --timeout=300s -n $NAMESPACE || true
    print_success "Service Books deployed"

    # Service Book Reviews (API)
    print_info "Deploying Service Book Reviews (API)..."
    kubectl apply -f $K8S_DIR/services/44-service-book-reviews.yaml
    print_info "Waiting for Service Book Reviews to be ready..."
    kubectl wait --for=condition=ready pod -l app=service-book-reviews --timeout=300s -n $NAMESPACE || true
    print_success "Service Book Reviews deployed"

    # Service Book Reviews (Frontend)
    print_info "Deploying Service Book Reviews (Frontend)..."
    kubectl apply -f $K8S_DIR/services/45-service-book-reviews-frontend.yaml
    print_info "Waiting for Service Reviews UI to be ready..."
    kubectl wait --for=condition=ready pod -l app=service-reviews-ui --timeout=300s -n $NAMESPACE || true
    print_success "Service Reviews UI deployed"

    print_success "All application services deployed"

    # Show status
    print_info "Application services status:"
    kubectl get pods -n $NAMESPACE -l 'app in (gateway,service-authors,service-email,service-books,service-book-reviews,service-reviews-ui)'
    echo ""
}

# Function to deploy ingress
deploy_ingress() {
    print_header "Deploying Ingress Controllers"

    print_info "Applying gateway ingress..."
    kubectl apply -f $K8S_DIR/ingress/50-gateway-ingress.yaml

    print_info "Applying books UI ingress..."
    kubectl apply -f $K8S_DIR/ingress/51-books-ui-ingress.yaml

    print_info "Applying reviews UI ingress..."
    kubectl apply -f $K8S_DIR/ingress/52-reviews-ui-ingress.yaml

    print_success "All ingress controllers deployed"

    # Show status
    print_info "Ingress status:"
    kubectl get ingress -n $NAMESPACE
    echo ""
}

# Function to check deployment status
check_status() {
    print_header "Deployment Status"

    print_info "Pods:"
    kubectl get pods -n $NAMESPACE
    echo ""

    print_info "Services:"
    kubectl get svc -n $NAMESPACE
    echo ""

    print_info "Ingress:"
    kubectl get ingress -n $NAMESPACE
    echo ""

    print_info "Certificates:"
    kubectl get managedcertificate -n $NAMESPACE
    echo ""

    print_info "Persistent Volumes:"
    kubectl get pvc -n $NAMESPACE
    echo ""
}

# Function to update existing deployments
update_deployments() {
    print_header "Updating Existing Deployments"

    print_info "This will perform a rolling update of all services..."
    read -p "Continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Update cancelled"
        exit 0
    fi

    # Update configs first
    deploy_configs

    # Update services
    print_info "Updating application services..."
    kubectl apply -f $K8S_DIR/services/

    # Restart deployments to pick up new configs
    print_info "Restarting deployments..."
    kubectl rollout restart deployment/gateway -n $NAMESPACE
    kubectl rollout restart deployment/service-authors -n $NAMESPACE
    kubectl rollout restart deployment/service-email -n $NAMESPACE
    kubectl rollout restart deployment/service-books -n $NAMESPACE
    kubectl rollout restart deployment/service-book-reviews -n $NAMESPACE
    kubectl rollout restart deployment/service-reviews-ui -n $NAMESPACE

    print_success "Update complete"
    echo ""
}

# Function to cleanup all resources
cleanup() {
    print_header "Cleanup"

    print_warning "This will DELETE all resources in namespace $NAMESPACE"
    print_warning "This action cannot be undone!"
    read -p "Are you sure? Type 'yes' to confirm: " -r
    echo
    if [[ ! $REPLY == "yes" ]]; then
        print_info "Cleanup cancelled"
        exit 0
    fi

    print_info "Deleting namespace $NAMESPACE..."
    kubectl delete namespace $NAMESPACE

    print_success "Cleanup complete"
    echo ""
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: ./deploy.sh [options]

Options:
    --full          Full deployment (secrets + infrastructure + services + ingress)
    --infra-only    Deploy only infrastructure (databases, message broker, etc.)
    --services-only Deploy only application services (requires infra running)
    --update        Update existing deployments (rolling update)
    --check         Check deployment status
    --cleanup       Delete all resources
    --help          Show this help message

Examples:
    # Full deployment
    ./deploy.sh --full

    # Deploy only infrastructure
    ./deploy.sh --infra-only

    # Check status
    ./deploy.sh --check

    # Update services
    ./deploy.sh --update

EOF
}

# Main script logic
main() {
    local mode="${1:---help}"

    case $mode in
        --full)
            check_prerequisites
            configure_kubectl
            create_namespace
            check_secrets
            deploy_secrets
            deploy_configs
            deploy_certificates
            deploy_infrastructure
            deploy_services
            deploy_ingress
            check_status
            print_header "Deployment Complete!"
            print_success "Your application is now deployed to GKE"
            print_info "Note: SSL certificates may take 10-15 minutes to provision"
            print_info "Check certificate status: kubectl get managedcertificate -n $NAMESPACE"
            ;;

        --infra-only)
            check_prerequisites
            configure_kubectl
            create_namespace
            check_secrets
            deploy_secrets
            deploy_configs
            deploy_certificates
            deploy_infrastructure
            print_header "Infrastructure Deployment Complete!"
            ;;

        --services-only)
            check_prerequisites
            configure_kubectl
            deploy_services
            deploy_ingress
            print_header "Services Deployment Complete!"
            ;;

        --update)
            check_prerequisites
            configure_kubectl
            update_deployments
            check_status
            ;;

        --check)
            check_prerequisites
            configure_kubectl
            check_status
            ;;

        --cleanup)
            check_prerequisites
            configure_kubectl
            cleanup
            ;;

        --help)
            show_usage
            ;;

        *)
            print_error "Unknown option: $mode"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"