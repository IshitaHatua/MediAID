import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (this.auth.isLoggedIn()) this.router.navigate([this.auth.getDashboardRoute()]);
  }

  features = [
    { title: 'Easy Registration',     desc: 'Sign up in minutes. Create your citizen profile and upload documents digitally.',                       icon: 'person_add',           bg: 'rgba(99,102,241,0.15)',   color: '#818cf8' },
    { title: 'Scheme Discovery',      desc: 'Browse available government healthcare schemes and check your eligibility criteria.',                    icon: 'manage_search',        bg: 'rgba(52,211,153,0.12)',   color: '#34d399' },
    { title: 'One-Click Enrollment',  desc: 'Enroll in healthcare programs with a streamlined single application process.',                            icon: 'assignment_turned_in', bg: 'rgba(251,191,36,0.12)',   color: '#fbbf24' },
    { title: 'Digital Claim Filing',  desc: 'Submit healthcare claims online, attach supporting documents, and track approval status.',               icon: 'receipt_long',         bg: 'rgba(239,68,68,0.12)',    color: '#f87171' },
    { title: 'Disbursement Tracking', desc: 'Monitor approved benefits and direct bank account disbursements with full transaction history.',         icon: 'account_balance',      bg: 'rgba(56,189,248,0.12)',   color: '#38bdf8' },
    { title: 'Audit & Compliance',    desc: 'Full audit trail for every action. Our compliance engine ensures transparency and accountability.',      icon: 'verified_user',        bg: 'rgba(167,139,250,0.12)',  color: '#a78bfa' },
  ];

  steps = [
    { title: 'Register & Verify',    desc: 'Create your account and complete profile verification with an officer.',                 icon: 'how_to_reg' },
    { title: 'Explore Schemes',      desc: 'Browse government healthcare schemes that match your profile and needs.',                 icon: 'search'     },
    { title: 'Enroll & File Claims', desc: 'Enroll in eligible schemes and submit claims with supporting documentation.',             icon: 'post_add'   },
    { title: 'Receive Benefits',     desc: 'Approved disbursements are transferred directly to your bank account.',                   icon: 'payments'   },
  ];

  stepColors = ['#6366f1', '#38bdf8', '#34d399', '#fbbf24'];

  pillars = [
    { title: 'Fully Paperless',      desc: 'End-to-end digital process. No physical forms or office visits.',  icon: 'eco',      bg: 'rgba(52,211,153,0.12)',  color: '#34d399' },
    { title: 'Real-Time Tracking',   desc: 'Live application status and instant updates.',                            icon: 'timeline', bg: 'rgba(56,189,248,0.12)',  color: '#38bdf8' },
    { title: 'Direct Bank Transfer', desc: 'Disbursements go straight to your bank account.',                         icon: 'payments', bg: 'rgba(251,191,36,0.12)',  color: '#fbbf24' },
    { title: 'Secure & Audited',     desc: 'Every action is logged and reviewed for compliance.',                     icon: 'shield',   bg: 'rgba(167,139,250,0.12)', color: '#a78bfa' },
  ];
// Explicit Type Annotation each with title and links array
  footerLinks: { title: string; links: { label: string; route?: string; href?: string }[] }[] = [
    {
      title: 'Portal',
      links: [
        { label: 'Home',     route: '/' },
        { label: 'Sign In',  route: '/auth/login' },
        { label: 'Register', route: '/auth/register' },
      ]
    },
    {
      title: 'Features',
      links: [
        { label: 'Scheme Enrollment', href: '#features' },
        { label: 'Claim Filing',      href: '#features' },
        { label: 'Disbursements',     href: '#features' },
      ]
    },
    {
      title: 'About',
      links: [
        { label: 'Our Mission', href: '#about' },
        { label: 'How It Works', href: '#how-it-works' },
      ]
    },
  ];
}
