export class User{
    id?: string
    name: string
    password: string
    email: string
    nick?: string
    birth?: string
    age?: number
    photoUrl?: string
    phone?: string
    about?: string

    constructor(
    id: string,
    name: string,
    password: string,
    email: string,
    nick: string,
    birth: string,
    age: number,
    photoUrl: string,
    phone: string,
    about: string
)
    {
        this.id=id;
        this.name=name;
        this.password=password;
        this.email=email;
        this.nick=nick;
        this.birth=birth;
        this.age=age;
        this.photoUrl=photoUrl;
        this.phone=phone;
        this.about = about;
    }


}
